-- Canonical quiz-attempt schema for the current Spring entity.
-- Run this in Supabase SQL Editor before deploying the backend that uses it.
-- The script preserves legacy records and is safe to run more than once.

begin;

do $$
begin
    if to_regclass('public.quiz_attempts') is null then
        raise exception 'Missing public.quiz_attempts; aborting migration.';
    end if;
end $$;

alter table public.quiz_attempts
    add column if not exists learner_id bigint,
    add column if not exists client_attempt_id text,
    add column if not exists selected_answer text,
    add column if not exists is_correct boolean,
    add column if not exists points_earned integer,
    add column if not exists stars_earned integer,
    add column if not exists attempted_at timestamp;

-- Backfill rows created by the legacy schema, when those columns are present.
do $$
begin
    if exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'quiz_attempts' and column_name = 'learner_user_id'
    ) then
        execute 'update public.quiz_attempts set learner_id = coalesce(learner_id, learner_user_id)';
    end if;

    if exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'quiz_attempts' and column_name = 'client_uuid'
    ) then
        execute 'update public.quiz_attempts set client_attempt_id = coalesce(client_attempt_id, client_uuid::text)';
    end if;

    if exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'quiz_attempts' and column_name = 'completed_at'
    ) then
        execute 'update public.quiz_attempts set attempted_at = coalesce(attempted_at, completed_at::timestamp)';
    end if;

    -- Legacy attempts do not retain an answer; score is the only safe source for correctness.
    execute 'update public.quiz_attempts set is_correct = coalesce(is_correct, score >= 100) where score is not null';
end $$;

-- Legacy writer-only fields must not block current entity inserts after data was copied.
do $$
declare
    legacy_column text;
begin
    foreach legacy_column in array array['learner_user_id', 'started_at', 'completed_at']
    loop
        if exists (
            select 1 from information_schema.columns
            where table_schema = 'public' and table_name = 'quiz_attempts' and column_name = legacy_column
        ) then
            execute format('alter table public.quiz_attempts alter column %I drop not null', legacy_column);
        end if;
    end loop;
end $$;

-- Drop a pre-existing global client-attempt unique constraint, if any.
do $$
declare
    constraint_name text;
begin
    for constraint_name in
        select c.conname
        from pg_constraint c
        join pg_class t on t.oid = c.conrelid
        join pg_namespace n on n.oid = t.relnamespace
        where n.nspname = 'public'
          and t.relname = 'quiz_attempts'
          and c.contype = 'u'
          and array_length(c.conkey::smallint[], 1) = 1
          and (
              select a.attname
              from pg_attribute a
              where a.attrelid = c.conrelid
                and a.attnum = (c.conkey::smallint[])[1]
          ) = 'client_attempt_id'
    loop
        execute format('alter table public.quiz_attempts drop constraint %I', constraint_name);
    end loop;
end $$;

-- Some deployments created the global unique rule as an index rather than a
-- named constraint. Remove only an index whose sole key is client_attempt_id.
do $$
declare
    index_name text;
begin
    for index_name in
        select index_class.relname
        from pg_index index_def
        join pg_class table_class on table_class.oid = index_def.indrelid
        join pg_namespace namespace on namespace.oid = table_class.relnamespace
        join pg_class index_class on index_class.oid = index_def.indexrelid
        where namespace.nspname = 'public'
          and table_class.relname = 'quiz_attempts'
          and index_def.indisunique
          and not index_def.indisprimary
          and index_def.indnatts = 1
          and (
              select a.attname
              from pg_attribute a
              where a.attrelid = index_def.indrelid
                and a.attnum = (index_def.indkey::smallint[])[1]
          ) = 'client_attempt_id'
    loop
        execute format('drop index public.%I', index_name);
    end loop;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint
        where conname = 'uk_quiz_attempt_learner_client'
          and conrelid = 'public.quiz_attempts'::regclass
    ) then
        alter table public.quiz_attempts
            add constraint uk_quiz_attempt_learner_client unique (learner_id, client_attempt_id);
    end if;
end $$;

create index if not exists idx_quiz_attempts_learner_attempted
    on public.quiz_attempts (learner_id, attempted_at desc);

commit;
