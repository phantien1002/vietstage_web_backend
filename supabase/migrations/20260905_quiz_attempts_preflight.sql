-- Read-only preflight for 20260905_quiz_attempts_canonical.sql.
-- Run first in Supabase SQL Editor and retain the result with the deployment record.

select column_name, data_type, is_nullable, column_default
from information_schema.columns
where table_schema = 'public' and table_name = 'quiz_attempts'
order by ordinal_position;

select conname, pg_get_constraintdef(oid) as definition
from pg_constraint
where conrelid = 'public.quiz_attempts'::regclass
order by conname;

select
    count(*) as total_rows,
    -- to_jsonb keeps this check valid even before canonical columns exist.
    count(*) filter (where (to_jsonb(qa) ->> 'learner_id') is null) as missing_learner_id,
    count(*) filter (where (to_jsonb(qa) ->> 'client_attempt_id') is null) as missing_client_attempt_id,
    count(*) filter (where (to_jsonb(qa) ->> 'attempted_at') is null) as missing_attempted_at
from public.quiz_attempts qa;

select indexname, indexdef
from pg_indexes
where schemaname = 'public' and tablename = 'quiz_attempts'
order by indexname;
