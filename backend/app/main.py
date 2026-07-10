from __future__ import annotations

import asyncio
from contextlib import asynccontextmanager
from datetime import datetime, timezone
from pathlib import Path

from fastapi import Depends, FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from sqlalchemy import select
from sqlalchemy.orm import Session

from .config import get_settings
from .database import Base, SessionLocal, engine, get_db
from .models import Content, DiamondEvidence, LearningRecord, QuizBankItem, Reminder, User, UserProfile
from .schemas import (
    AdminContentItem,
    AdminQuizItem,
    AgentRequest,
    AgentResponse,
    AuthRequest,
    AuthResponse,
    CloudStateResponse,
    DiamondStatus,
    EvidenceRequest,
    EvidenceReviewRequest,
    GenericResponse,
    QuizRequest,
    QuizResponse,
    ReminderRequest,
    ReminderResponse,
    SyncRequest,
    SyncResponse,
    YouTubeSyncRequest,
)
from .security import create_access_token, get_current_user, hash_password, require_admin, verify_password
from .services import (
    answer_agent,
    embed_missing_chunks,
    generate_quiz,
    import_content_file,
    import_knowledge_file,
    import_quiz_file,
    seed_contents,
    seed_knowledge,
    seed_quizzes,
    sync_youtube,
)

settings = get_settings()
STATIC_DIR = Path(__file__).resolve().parents[1] / "static"


async def scheduled_youtube_sync() -> None:
    interval = max(settings.youtube_sync_hours, 0) * 3600
    if interval <= 0 or not settings.youtube_api_key:
        return
    while True:
        await asyncio.sleep(interval)
        try:
            with SessionLocal() as db:
                await asyncio.to_thread(
                    sync_youtube,
                    db,
                    settings.youtube_query_list,
                    settings.youtube_sync_max_results,
                )
        except Exception as exc:  # keep the scheduler alive; deployment logs capture the failure
            print(f"BluePath scheduled YouTube sync failed: {exc}")


@asynccontextmanager
async def lifespan(_: FastAPI):
    settings.validate_runtime()
    Base.metadata.create_all(bind=engine)
    with SessionLocal() as db:
        seed_contents(db)
        seed_knowledge(db)
        seed_quizzes(db)
        bootstrap_admin(db)
    scheduler = asyncio.create_task(scheduled_youtube_sync())
    try:
        yield
    finally:
        scheduler.cancel()
        try:
            await scheduler
        except asyncio.CancelledError:
            pass


app = FastAPI(title=settings.app_name, version="1.1.0", lifespan=lifespan)
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origin_list,
    allow_credentials=settings.cors_origin_list != ["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
def health() -> dict:
    return {
        "status": "ok",
        "environment": settings.environment,
        "llmEnabled": settings.llm_enabled,
        "embeddingEnabled": bool(settings.embedding_model),
    }


@app.get("/admin", include_in_schema=False)
def admin_dashboard() -> FileResponse:
    return FileResponse(STATIC_DIR / "admin.html")


@app.post("/api/v1/auth/register", response_model=AuthResponse)
def register(request: AuthRequest, db: Session = Depends(get_db)) -> AuthResponse:
    email = request.email.lower().strip()
    if db.scalar(select(User).where(User.email == email)):
        raise HTTPException(status_code=409, detail="Email is already registered")
    user = User(
        email=email,
        password_hash=hash_password(request.password),
        display_name=email.split("@")[0],
        guardian_email=str(request.guardianEmail) if request.guardianEmail else None,
        guardian_consent=False,
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return auth_response(user)


@app.post("/api/v1/auth/login", response_model=AuthResponse)
def login(request: AuthRequest, db: Session = Depends(get_db)) -> AuthResponse:
    user = db.scalar(select(User).where(User.email == request.email.lower().strip()))
    if not user or not verify_password(request.password, user.password_hash):
        raise HTTPException(status_code=401, detail="Incorrect email or password")
    if not user.is_active:
        raise HTTPException(status_code=403, detail="Account is disabled")
    return auth_response(user)


@app.post("/api/v1/ai/quiz", response_model=QuizResponse)
def ai_quiz(
    request: QuizRequest,
    db: Session = Depends(get_db),
    _: User = Depends(get_current_user),
) -> QuizResponse:
    return generate_quiz(db, request)


@app.post("/api/v1/ai/agent", response_model=AgentResponse)
def ai_agent(
    request: AgentRequest,
    db: Session = Depends(get_db),
    _: User = Depends(get_current_user),
) -> AgentResponse:
    return answer_agent(db, request)


@app.get("/api/v1/catalog", response_model=list[AdminContentItem])
def learner_catalog(
    _: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[AdminContentItem]:
    items = db.scalars(select(Content).order_by(Content.content_type, Content.updated_at.desc()).limit(500))
    return [content_to_schema(item) for item in items]


@app.get("/api/v1/sync", response_model=CloudStateResponse)
def get_cloud_state(
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> CloudStateResponse:
    profile = db.get(UserProfile, user.id)
    return CloudStateResponse(
        snapshot=profile.snapshot if profile else {},
        diamondStatus=diamond_status_for(db, user),
    )


@app.post("/api/v1/sync", response_model=SyncResponse)
def sync_progress(
    request: SyncRequest,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> SyncResponse:
    profile = db.get(UserProfile, user.id) or UserProfile(user_id=user.id)
    profile.snapshot = request.snapshot
    db.add(profile)

    user.guardian_consent = bool(request.snapshot.get("guardianConsent", user.guardian_consent))
    guardian_email = str(request.snapshot.get("guardianEmail", "")).strip()
    if guardian_email:
        user.guardian_email = guardian_email

    for record in request.learningRecords:
        client_id = str(record.id)
        exists = db.scalar(
            select(LearningRecord).where(
                LearningRecord.user_id == user.id,
                LearningRecord.client_record_id == client_id,
            )
        )
        if exists:
            continue
        db.add(
            LearningRecord(
                user_id=user.id,
                client_record_id=client_id,
                record_type=record.recordType,
                target_id=record.targetId,
                title=record.title,
                status=record.status,
                client_updated_at=record.updatedAt,
            )
        )
    db.commit()
    status = diamond_status_for(db, user)
    return SyncResponse(
        message="Cloud learning records are up to date.",
        syncedAt=datetime.now(timezone.utc).isoformat(),
        diamondStatus=status,
        snapshot=profile.snapshot,
    )


@app.post("/api/v1/diamond/evidence", response_model=GenericResponse)
def submit_diamond_evidence(
    request: EvidenceRequest,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> GenericResponse:
    evidence = db.scalar(
        select(DiamondEvidence).where(
            DiamondEvidence.user_id == user.id,
            DiamondEvidence.evidence_type == request.evidenceType,
        )
    ) or DiamondEvidence(user_id=user.id, evidence_type=request.evidenceType, title="", evidence_url="")
    evidence.title = request.title
    evidence.evidence_url = str(request.evidenceUrl)
    evidence.status = "pending"
    evidence.review_note = ""
    evidence.reviewed_at = None
    db.add(evidence)
    db.commit()
    return GenericResponse(message="Evidence was submitted for administrator review.")


@app.get("/api/v1/diamond/status", response_model=DiamondStatus)
def diamond_status(
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> DiamondStatus:
    return diamond_status_for(db, user)


@app.post("/api/v1/reminders", response_model=ReminderResponse)
def create_reminder(
    request: ReminderRequest,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> ReminderResponse:
    reminder = Reminder(
        user_id=user.id,
        title=request.title,
        remind_at=request.remindAt,
        reminder_type=request.reminderType,
    )
    db.add(reminder)
    db.commit()
    db.refresh(reminder)
    return ReminderResponse(
        id=reminder.id,
        title=reminder.title,
        remindAt=reminder.remind_at,
        reminderType=reminder.reminder_type,
        enabled=reminder.enabled,
    )


@app.get("/api/v1/reminders", response_model=list[ReminderResponse])
def list_reminders(
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[ReminderResponse]:
    reminders = list(db.scalars(select(Reminder).where(Reminder.user_id == user.id).order_by(Reminder.remind_at)))
    return [
        ReminderResponse(
            id=item.id,
            title=item.title,
            remindAt=item.remind_at,
            reminderType=item.reminder_type,
            enabled=item.enabled,
        )
        for item in reminders
    ]


@app.get("/api/v1/admin/content", response_model=list[AdminContentItem])
def admin_list_content(
    content_type: str | None = None,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> list[AdminContentItem]:
    statement = select(Content).order_by(Content.updated_at.desc())
    if content_type:
        statement = statement.where(Content.content_type == content_type)
    return [content_to_schema(item) for item in db.scalars(statement.limit(300))]


@app.post("/api/v1/admin/content", response_model=AdminContentItem)
def admin_save_content(
    request: AdminContentItem,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> AdminContentItem:
    item = db.get(Content, request.id) or Content(id=request.id, title=request.title)
    item.title = request.title
    item.content_type = request.contentType
    item.source = request.source
    item.url = request.url
    item.difficulty = request.difficulty
    item.required_tier = request.requiredTier
    item.topic = request.topic
    item.career_tag = request.careerTag
    item.minutes = request.minutes
    item.metadata_json = {
        **(item.metadata_json or {}),
        "startAt": request.startAt,
        "endAt": request.endAt,
        "target": request.target,
        "method": request.method,
        "category": request.category,
        "description": request.description,
    }
    db.add(item)
    db.commit()
    db.refresh(item)
    return content_to_schema(item)


@app.delete("/api/v1/admin/content/{content_id}", response_model=GenericResponse)
def admin_delete_content(
    content_id: str,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> GenericResponse:
    item = db.get(Content, content_id)
    if item is None:
        raise HTTPException(status_code=404, detail="Content not found")
    db.delete(item)
    db.commit()
    return GenericResponse(message="Content was deleted.")


@app.get("/api/v1/admin/quizzes", response_model=list[AdminQuizItem])
def admin_list_quizzes(
    tier: str | None = None,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> list[AdminQuizItem]:
    statement = select(QuizBankItem).order_by(QuizBankItem.tier, QuizBankItem.id)
    if tier:
        statement = statement.where(QuizBankItem.tier == tier)
    return [quiz_to_schema(item) for item in db.scalars(statement.limit(500))]


@app.post("/api/v1/admin/quizzes", response_model=AdminQuizItem)
def admin_save_quiz(
    request: AdminQuizItem,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> AdminQuizItem:
    item = db.get(QuizBankItem, request.id) or QuizBankItem(id=request.id, tier=request.tier, question=request.question, answer_index=request.answerIndex)
    item.tier = request.tier
    item.topic = request.topic
    item.question = request.question
    item.options = request.options
    item.answer_index = request.answerIndex
    item.explanation = request.explanation
    item.source_title = request.sourceTitle
    item.source_url = request.sourceUrl
    item.active = request.active
    db.add(item)
    db.commit()
    db.refresh(item)
    return quiz_to_schema(item)


@app.delete("/api/v1/admin/quizzes/{quiz_id}", response_model=GenericResponse)
def admin_delete_quiz(
    quiz_id: str,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> GenericResponse:
    item = db.get(QuizBankItem, quiz_id)
    if item is None:
        raise HTTPException(status_code=404, detail="Quiz not found")
    db.delete(item)
    db.commit()
    return GenericResponse(message="Quiz was deleted.")


@app.post("/api/v1/admin/content/upload")
def admin_upload_content(
    file: UploadFile = File(...),
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> dict:
    imported, errors = import_content_file(db, file)
    return {"imported": imported, "errors": errors}


@app.post("/api/v1/admin/knowledge/upload")
def admin_upload_knowledge(
    file: UploadFile = File(...),
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> dict:
    imported, errors = import_knowledge_file(db, file)
    return {"imported": imported, "errors": errors, "nextStep": "Run RAG embedding update."}


@app.post("/api/v1/admin/quizzes/upload")
def admin_upload_quizzes(
    file: UploadFile = File(...),
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> dict:
    imported, errors = import_quiz_file(db, file)
    return {"imported": imported, "errors": errors}


@app.post("/api/v1/admin/youtube/sync")
def admin_youtube_sync(
    request: YouTubeSyncRequest,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> dict:
    imported = sync_youtube(db, request.queries, request.maxResultsPerQuery)
    return {"imported": imported}


@app.post("/api/v1/admin/rag/embed")
def admin_embed_knowledge(
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> dict:
    return {"embedded": embed_missing_chunks(db)}


@app.get("/api/v1/admin/diamond/pending")
def admin_pending_diamond(
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> list[dict]:
    rows = list(db.scalars(select(DiamondEvidence).where(DiamondEvidence.status == "pending")))
    return [
        {
            "id": item.id,
            "userId": item.user_id,
            "evidenceType": item.evidence_type,
            "title": item.title,
            "evidenceUrl": item.evidence_url,
            "status": item.status,
        }
        for item in rows
    ]


@app.post("/api/v1/admin/diamond/{evidence_id}/review", response_model=GenericResponse)
def admin_review_diamond(
    evidence_id: str,
    request: EvidenceReviewRequest,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
) -> GenericResponse:
    evidence = db.get(DiamondEvidence, evidence_id)
    if evidence is None:
        raise HTTPException(status_code=404, detail="Evidence not found")
    evidence.status = request.status
    evidence.review_note = request.reviewNote
    evidence.reviewed_at = datetime.now(timezone.utc)
    db.commit()
    return GenericResponse(message="Evidence review was saved.")


def content_to_schema(item: Content) -> AdminContentItem:
    metadata = item.metadata_json or {}
    return AdminContentItem(
        id=item.id,
        title=item.title,
        contentType=item.content_type,
        source=item.source,
        url=item.url,
        difficulty=item.difficulty,
        requiredTier=item.required_tier,
        topic=item.topic,
        careerTag=item.career_tag,
        minutes=item.minutes,
        startAt=str(metadata.get("startAt", "")),
        endAt=str(metadata.get("endAt", "")),
        target=str(metadata.get("target", "전체")),
        method=str(metadata.get("method", "")),
        category=str(metadata.get("category", "")),
        description=str(metadata.get("description", "")),
    )


def quiz_to_schema(item: QuizBankItem) -> AdminQuizItem:
    return AdminQuizItem(
        id=item.id,
        tier=item.tier,
        topic=item.topic,
        question=item.question,
        options=list(item.options),
        answerIndex=item.answer_index,
        explanation=item.explanation,
        sourceTitle=item.source_title,
        sourceUrl=item.source_url,
        active=item.active,
    )


def auth_response(user: User) -> AuthResponse:
    return AuthResponse(
        accessToken=create_access_token(user),
        email=user.email,
        displayName=user.display_name,
    )


def diamond_status_for(db: Session, user: User) -> DiamondStatus:
    profile = db.get(UserProfile, user.id)
    snapshot = profile.snapshot if profile else {}
    advanced = bool(snapshot.get("diamondAdvancedQuizPassed", False))
    evidence = list(db.scalars(select(DiamondEvidence).where(DiamondEvidence.user_id == user.id)))
    statuses = {item.evidence_type: item.status for item in evidence}
    certification = statuses.get("certification", "not_submitted")
    project = statuses.get("project", "not_submitted")
    eligible = advanced and certification == "approved" and project == "approved"
    message = "Diamond pathway complete." if eligible else "Complete the advanced quiz and receive approval for both evidence items."
    return DiamondStatus(
        advancedQuizPassed=advanced,
        certificationStatus=certification,
        projectStatus=project,
        eligible=eligible,
        message=message,
    )


def bootstrap_admin(db: Session) -> None:
    if not settings.admin_email or not settings.admin_password:
        return
    email = settings.admin_email.lower().strip()
    user = db.scalar(select(User).where(User.email == email))
    if user:
        if user.role != "super_admin":
            user.role = "super_admin"
            db.commit()
        return
    db.add(
        User(
            email=email,
            password_hash=hash_password(settings.admin_password),
            display_name="BluePath Admin",
            role="super_admin",
        )
    )
    db.commit()
