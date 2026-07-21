CREATE TABLE IF NOT EXISTS community_reports (
  id VARCHAR(36) PRIMARY KEY,
  reporter_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  target_type VARCHAR(20) NOT NULL,
  target_id VARCHAR(36) NOT NULL,
  reason VARCHAR(500) NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'pending',
  reviewed_by VARCHAR(36) REFERENCES users(id) ON DELETE SET NULL,
  review_note TEXT NOT NULL DEFAULT '',
  reviewed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_community_report UNIQUE (reporter_id, target_type, target_id),
  CONSTRAINT ck_community_report_target_type CHECK (target_type IN ('post', 'comment', 'user'))
);
CREATE INDEX IF NOT EXISTS ix_community_reports_status ON community_reports(status, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_community_reports_reporter_id ON community_reports(reporter_id);
CREATE INDEX IF NOT EXISTS ix_community_reports_reviewed_by ON community_reports(reviewed_by);

CREATE TABLE IF NOT EXISTS community_blocks (
  id VARCHAR(36) PRIMARY KEY,
  blocker_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  blocked_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_community_block UNIQUE (blocker_id, blocked_id),
  CONSTRAINT ck_community_block_not_self CHECK (blocker_id <> blocked_id)
);
CREATE INDEX IF NOT EXISTS ix_community_blocks_blocker_id ON community_blocks(blocker_id);
CREATE INDEX IF NOT EXISTS ix_community_blocks_blocked_id ON community_blocks(blocked_id);
