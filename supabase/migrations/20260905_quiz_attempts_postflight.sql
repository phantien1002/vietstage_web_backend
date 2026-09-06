-- Read-only postflight for 20260905_quiz_attempts_canonical.sql.
-- Run immediately after the migration in Supabase SQL Editor.

select
    count(*) as total_rows,
    count(*) filter (where learner_id is null) as missing_learner_id,
    count(*) filter (where client_attempt_id is null) as missing_client_attempt_id,
    count(*) filter (where attempted_at is null) as missing_attempted_at
from public.quiz_attempts;

select conname, pg_get_constraintdef(oid) as definition
from pg_constraint
where conrelid = 'public.quiz_attempts'::regclass
  and conname = 'uk_quiz_attempt_learner_client';

select indexname, indexdef
from pg_indexes
where schemaname = 'public'
  and tablename = 'quiz_attempts'
  and indexname in ('uk_quiz_attempt_learner_client', 'idx_quiz_attempts_learner_attempted')
order by indexname;
