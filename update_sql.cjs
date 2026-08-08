const fs = require('fs');
let sql = fs.readFileSync('vietstage_full_reset_v2.sql', 'utf8');

const target = `    received_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_practice_time CHECK (completed_at >= started_at),`;
const replacement = `    session_id          BIGINT,
    stars               INT,
    points_earned       INT,
    sync_status         VARCHAR(50),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_practice_time CHECK (completed_at >= started_at),`;

if(sql.includes(target)) {
    sql = sql.replace(target, replacement);
    sql = sql.replace('learner_user_id     BIGINT', 'learner_id          BIGINT');
    sql = sql.replace('composite_score     NUMERIC', 'total_score         NUMERIC');
    sql = sql.replace('started_at          TIMESTAMPTZ NOT NULL', 'started_at          TIMESTAMPTZ');
    sql = sql.replace('completed_at        TIMESTAMPTZ NOT NULL', 'completed_at        TIMESTAMPTZ');
    fs.writeFileSync('vietstage_full_reset_v2.sql', sql, 'utf8');
    console.log('Fixed sql');
} else {
    console.log('Not found');
}
