import fs from 'fs';

function fixSqlFile(filename) {
    if (!fs.existsSync(filename)) return;
    let content = fs.readFileSync(filename, 'utf8');
    
    content = content.replace(
        /user_id\s+BIGSERIAL PRIMARY KEY,\s*role_id/g,
        'user_id        BIGSERIAL PRIMARY KEY,\n      user_code      VARCHAR(50) UNIQUE,\n      role_id'
    );

    if (content.includes('INSERT INTO users (email, password_hash')) {
        content = content.replace(
            'INSERT INTO users (email, password_hash, full_name, role_id, avatar_url, is_active, created_at, updated_at) VALUES',
            'INSERT INTO users (user_code, email, password_hash, full_name, role_id, avatar_url, is_active, created_at, updated_at) VALUES'
        );
        content = content.replace("('admin@vietstage.com'", "('VS-2024-001', 'admin@vietstage.com'");
        content = content.replace("('quang@vietstage.com'", "('VS-2024-002', 'quang@vietstage.com'");
        content = content.replace("('dantranh.master@vietstage.com'", "('VS-2024-003', 'dantranh.master@vietstage.com'");
        content = content.replace("('saotruc.guru@vietstage.com'", "('VS-2024-004', 'saotruc.guru@vietstage.com'");
        content = content.replace("('danbau.pro@vietstage.com'", "('VS-2024-005', 'danbau.pro@vietstage.com'");
        content = content.replace("('learner1@gmail.com'", "('VS-2024-006', 'learner1@gmail.com'");
        content = content.replace("('learner2@gmail.com'", "('VS-2024-007', 'learner2@gmail.com'");
    }

    fs.writeFileSync(filename, content, 'utf8');
}

fixSqlFile('vietstage.sql');
fixSqlFile('seed_data.sql');
fixSqlFile('vietstage_full_reset_v2.sql');
console.log("Done");
