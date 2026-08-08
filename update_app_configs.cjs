const fs = require('fs');
let sql = fs.readFileSync('vietstage_full_reset_v2.sql', 'utf8');

const target = `CREATE TABLE app_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key         VARCHAR(120) NOT NULL UNIQUE,
    config_value       JSONB NOT NULL,
    description        TEXT,
    updated_by_user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);`;

const replacement = `CREATE TABLE app_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key         VARCHAR(120) NOT NULL UNIQUE,
    config_value       VARCHAR(255) NOT NULL,
    config_group       VARCHAR(50),
    description        TEXT,
    value_type         VARCHAR(20),
    min_value          DOUBLE PRECISION,
    max_value          DOUBLE PRECISION,
    step_value         DOUBLE PRECISION,
    options            TEXT,
    default_value      VARCHAR(255),
    updated_by         BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);`;

if (sql.includes(target)) {
    sql = sql.replace(target, replacement);
    fs.writeFileSync('vietstage_full_reset_v2.sql', sql, 'utf8');
    console.log('Updated successfully');
} else {
    console.log('Target not found');
}
