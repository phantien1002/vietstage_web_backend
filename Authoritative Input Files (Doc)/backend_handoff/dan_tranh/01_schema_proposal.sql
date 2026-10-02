-- PostgreSQL reference design ONLY. Review/map to existing backend entities.
-- NOT a production migration. No existing table is altered or deleted.
-- Use a separate review database. Run 02_seed_snapshot.sql after this file.
BEGIN;
CREATE SCHEMA IF NOT EXISTS dan_tranh_review;
CREATE TABLE IF NOT EXISTS dan_tranh_review.skill_level (
 code text PRIMARY KEY CHECK (code IN ('BEGINNER','INTERMEDIATE','ADVANCED')),
 name text NOT NULL, order_index integer NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS dan_tranh_review.lesson (
 lesson_code text PRIMARY KEY,
 instrument_code text NOT NULL DEFAULT 'dan_tranh' CHECK (instrument_code = 'dan_tranh'),
 skill_level_code text REFERENCES dan_tranh_review.skill_level(code) ON DELETE RESTRICT,
 title text NOT NULL, order_index integer NOT NULL, display_number text NOT NULL,
 legacy_level integer NOT NULL, in_current_roadmap boolean NOT NULL,
 -- Publication is separate from review approval. Seed stays hidden/draft.
 approval_status text NOT NULL DEFAULT 'DRAFT',
 is_visible boolean NOT NULL DEFAULT false,
 hidden_at timestamptz, hidden_reason text,
 revision integer NOT NULL DEFAULT 1 CHECK (revision > 0),
 source_snapshot jsonb NOT NULL,
 updated_at timestamptz NOT NULL DEFAULT now()
);
-- Preserve video/practice/quiz keys individually: do not sum their stars into
-- a new lesson reward without a migration policy and deduplication audit.
CREATE TABLE IF NOT EXISTS dan_tranh_review.lesson_legacy_key (
 lesson_code text NOT NULL REFERENCES dan_tranh_review.lesson ON DELETE RESTRICT,
 activity_kind text NOT NULL CHECK (activity_kind IN ('practice','video','quiz')),
 legacy_code text NOT NULL,
 PRIMARY KEY (lesson_code, activity_kind)
);
CREATE TABLE IF NOT EXISTS dan_tranh_review.lesson_revision (
 lesson_code text NOT NULL REFERENCES dan_tranh_review.lesson ON DELETE RESTRICT,
 revision integer NOT NULL, content_document jsonb NOT NULL,
 created_at timestamptz NOT NULL DEFAULT now(),
 PRIMARY KEY (lesson_code, revision)
);
CREATE TABLE IF NOT EXISTS dan_tranh_review.lesson_activity (
 activity_code text PRIMARY KEY,
 lesson_code text NOT NULL, revision integer NOT NULL,
 order_index integer NOT NULL,
 activity_type text NOT NULL CHECK (activity_type IN ('TEACHER_SPEECH','VIDEO','DEMO','PRACTICE','QUIZ','MINIGAME','WAIT_FOR_NOTE','WAIT_FOR_GESTURE')),
 content_text text, config jsonb NOT NULL DEFAULT '{}',
 FOREIGN KEY (lesson_code, revision) REFERENCES dan_tranh_review.lesson_revision ON DELETE RESTRICT,
 UNIQUE (lesson_code, revision, order_index)
);
CREATE TABLE IF NOT EXISTS dan_tranh_review.exercise_config (
 exercise_code text PRIMARY KEY,
 activity_code text NOT NULL UNIQUE REFERENCES dan_tranh_review.lesson_activity ON DELETE RESTRICT,
 practice_mode text NOT NULL,
 schema_version integer NOT NULL,
 pass_threshold numeric CHECK (pass_threshold BETWEEN 0 AND 100),
 -- Keep nullable until the actual scoring policy is approved, never seed 80 arbitrarily.
 practice_config jsonb NOT NULL,
 recognition_profile_version text NOT NULL
);
-- Raw runtime steps are staged separately because they include speak/highlight/
-- interactive actions and legacy keys not all represented by visible cards.
CREATE TABLE IF NOT EXISTS dan_tranh_review.runtime_dialogue (
 legacy_code text NOT NULL, order_index integer NOT NULL,
 action text NOT NULL, content_text text, raw_step jsonb NOT NULL,
 PRIMARY KEY (legacy_code, order_index)
);
CREATE TABLE IF NOT EXISTS dan_tranh_review.runtime_profile (
 profile_key text PRIMARY KEY, config jsonb NOT NULL
);
-- Reference extensions to existing progress/attempt/wallet tables, not replacements.
CREATE TABLE IF NOT EXISTS dan_tranh_review.lesson_completion (
 learner_id bigint NOT NULL,
 lesson_code text NOT NULL REFERENCES dan_tranh_review.lesson ON DELETE RESTRICT,
 best_score numeric CHECK (best_score BETWEEN 0 AND 100),
 stars integer NOT NULL DEFAULT 0 CHECK (stars BETWEEN 0 AND 3),
 completed_at timestamptz,
 PRIMARY KEY (learner_id, lesson_code)
);
CREATE TABLE IF NOT EXISTS dan_tranh_review.completion_attempt (
 learner_id bigint NOT NULL, client_attempt_id text NOT NULL,
 lesson_code text NOT NULL, revision integer NOT NULL,
 score numeric NOT NULL CHECK (score BETWEEN 0 AND 100),
 completed_at timestamptz NOT NULL, received_at timestamptz NOT NULL DEFAULT now(),
 PRIMARY KEY (learner_id, client_attempt_id),
 FOREIGN KEY (lesson_code, revision) REFERENCES dan_tranh_review.lesson_revision ON DELETE RESTRICT
);
CREATE TABLE IF NOT EXISTS dan_tranh_review.star_ledger (
 learner_id bigint NOT NULL, event_key text NOT NULL,
 lesson_code text REFERENCES dan_tranh_review.lesson ON DELETE RESTRICT,
 delta integer NOT NULL, reason text NOT NULL,
 created_at timestamptz NOT NULL DEFAULT now(),
 PRIMARY KEY (learner_id, event_key)
);
-- Production: add user FK, actor/audit fields, role authorization, immutable
-- revision/ledger enforcement and atomic completion+reward transaction.
-- No DELETE/soft-delete endpoint for lessons; hide must preserve all rows.
COMMIT;
