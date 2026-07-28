const fs = require('fs');
let sql = fs.readFileSync('vietstage.sql', 'utf8');

// 1. Drop audit_logs in DROP TABLE section
sql = sql.replace('DROP TABLE IF EXISTS roles CASCADE;', 'DROP TABLE IF EXISTS audit_logs CASCADE;\nDROP TABLE IF EXISTS roles CASCADE;');

// 2. Add avatar_url to users
sql = sql.replace('    full_name      VARCHAR(150) NOT NULL,', '    full_name      VARCHAR(150) NOT NULL,\n    avatar_url     TEXT,');

// 3. Remove avatar_url from learner_profiles
sql = sql.replace('    avatar_url             TEXT,\n', '');

// 4. Add audit_logs table definition
const auditLogsTable = `
CREATE TABLE audit_logs (
    log_id          BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    action_type     VARCHAR(50) NOT NULL,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       VARCHAR(100),
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_logs_user_time ON audit_logs(user_id, created_at DESC);`;

sql = sql.replace('CREATE TABLE instructor_profiles', auditLogsTable + '\n\nCREATE TABLE instructor_profiles');

fs.writeFileSync('vietstage.sql', sql, 'utf8');

const seedData = fs.readFileSync('seed_data.sql', 'utf8');
fs.writeFileSync('vietstage_full_reset_v2.sql', sql + '\n\n' + seedData, 'utf8');
