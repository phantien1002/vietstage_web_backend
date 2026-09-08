-- Lesson-level assessment sessions. Legacy quiz_attempts/minigame_attempts are untouched.
begin;

create table if not exists public.lesson_assessment_sessions (
    id bigserial primary key,
    learner_id bigint not null references public.users(user_id),
    lesson_id bigint not null references public.lessons(lesson_id),
    client_session_id text not null,
    status text not null default 'CONFIRMED',
    score numeric(8,2) not null,
    max_score numeric(8,2) not null,
    accuracy numeric(5,2) not null,
    stars_earned integer not null default 0,
    points_earned integer not null default 0,
    started_at timestamp not null,
    completed_at timestamp not null,
    constraint uk_assessment_session_learner_client unique (learner_id, client_session_id)
);

create table if not exists public.lesson_assessment_quiz_answers (
    id bigserial primary key,
    session_id bigint not null references public.lesson_assessment_sessions(id) on delete cascade,
    quiz_id bigint not null references public.quizzes(id),
    selected_answer text not null,
    is_correct boolean not null,
    score numeric(8,2) not null,
    max_score numeric(8,2) not null,
    constraint uk_assessment_session_quiz unique (session_id, quiz_id)
);

create table if not exists public.lesson_assessment_minigame_results (
    id bigserial primary key,
    session_id bigint not null references public.lesson_assessment_sessions(id) on delete cascade,
    minigame_id bigint not null references public.minigame_challenges(id),
    score numeric(8,2) not null,
    max_score numeric(8,2) not null,
    started_at timestamp,
    completed_at timestamp,
    constraint uk_assessment_session_minigame unique (session_id, minigame_id)
);

create index if not exists idx_assessment_sessions_learner_completed
    on public.lesson_assessment_sessions (learner_id, completed_at desc);
commit;
