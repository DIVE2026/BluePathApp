-- BluePath production DB draft schema (PostgreSQL)
-- 모바일 앱 prototype은 SharedPreferences + static repository로 동작합니다.
-- 실제 서비스화 시 아래 schema를 Supabase/PostgreSQL/FastAPI와 연결하세요.

CREATE TABLE users (
  id UUID PRIMARY KEY,
  email TEXT UNIQUE,
  role TEXT NOT NULL DEFAULT 'learner', -- learner, parent, institution_admin, super_admin
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE user_profiles (
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  age_group TEXT,
  interest TEXT,
  goal TEXT,
  level TEXT,
  persona TEXT,
  xp INT DEFAULT 0,
  tier TEXT DEFAULT '브론즈',
  updated_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY(user_id)
);

CREATE TABLE contents (
  id TEXT PRIMARY KEY,
  title TEXT NOT NULL,
  content_type TEXT NOT NULL, -- video, article, book, paper, course
  source TEXT,
  url TEXT,
  difficulty TEXT,
  required_tier TEXT,
  topic TEXT,
  career_tag TEXT,
  minutes INT,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE programs (
  id TEXT PRIMARY KEY,
  title TEXT NOT NULL,
  target TEXT,
  start_date DATE,
  end_date DATE,
  method TEXT,
  topic TEXT,
  description TEXT
);

CREATE TABLE events (
  id TEXT PRIMARY KEY,
  title TEXT NOT NULL,
  target TEXT,
  category TEXT,
  start_date DATE,
  end_date DATE,
  description TEXT
);

CREATE TABLE careers (
  id TEXT PRIMARY KEY,
  title TEXT NOT NULL,
  field TEXT,
  description TEXT,
  recommended_tier TEXT
);

CREATE TABLE ncs_units (
  id TEXT PRIMARY KEY,
  career_id TEXT REFERENCES careers(id),
  name TEXT NOT NULL,
  definition TEXT,
  knowledge TEXT,
  skill TEXT,
  attitude TEXT
);

CREATE TABLE quizzes (
  id TEXT PRIMARY KEY,
  tier TEXT,
  topic TEXT,
  question TEXT,
  options JSONB,
  answer_index INT,
  explanation TEXT
);

CREATE TABLE quiz_attempts (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  quiz_id TEXT REFERENCES quizzes(id),
  selected_index INT,
  is_correct BOOLEAN,
  earned_xp INT,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE progress (
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  content_id TEXT REFERENCES contents(id),
  status TEXT, -- started, completed
  progress_percent INT DEFAULT 0,
  updated_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY(user_id, content_id)
);

CREATE TABLE bookmarks (
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  target_type TEXT NOT NULL, -- content, program, event, career
  target_id TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY(user_id, target_type, target_id)
);

CREATE INDEX idx_contents_topic_tier ON contents(topic, required_tier, difficulty);
CREATE INDEX idx_programs_target_date ON programs(target, start_date);
CREATE INDEX idx_events_category_date ON events(category, start_date);
CREATE INDEX idx_profiles_interest_tier ON user_profiles(interest, tier);
