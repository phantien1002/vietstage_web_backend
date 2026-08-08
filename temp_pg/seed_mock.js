const { Client } = require('pg');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        
        console.log("Seeding data...");
        
        // 1. Get instructor
        const instructorRes = await client.query("SELECT user_id FROM users WHERE email='dantranh.master@vietstage.com';");
        if (instructorRes.rows.length === 0) throw new Error("Instructor not found");
        const instructorId = instructorRes.rows[0].user_id;

        // 2. Get skill level
        const skillRes = await client.query("SELECT skill_level_id FROM skill_levels LIMIT 1;");
        const skillId = skillRes.rows[0].skill_level_id;

        // 3. Create Lessons
        const ts = Date.now();
        const lesson1Res = await client.query(`
            INSERT INTO lessons (lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, status, order_index)
            VALUES ('LESSON_DT_${ts % 10000}', 1, $1, $2, 'Bài học Đàn Tranh cơ bản 2', 'Học những nốt đầu tiên trên Đàn Tranh', 'APPROVED', $3)
            RETURNING lesson_id;
        `, [skillId, instructorId, ts % 10000]);
        const lesson1Id = lesson1Res.rows[0].lesson_id;

        const lesson2Res = await client.query(`
            INSERT INTO lessons (lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, status, order_index)
            VALUES ('LESSON_SAO_${ts % 10000}', 3, $1, $2, 'Bài học Sáo trúc cơ bản 2', 'Luyện hơi thở và thổi sáo cơ bản', 'APPROVED', $3)
            RETURNING lesson_id;
        `, [skillId, instructorId, ts % 10000]);
        const lesson2Id = lesson2Res.rows[0].lesson_id;

        console.log("Created lessons:", lesson1Id, lesson2Id);

        // 4. Create Learners
        const learnerIds = [];
        for (let i = 1; i <= 5; i++) {
            const email = `test.learner${ts}_${i}@vietstage.com`;
            const code = `LEARNER_TEST_${ts}_${i}`;
            const name = `Học viên ${i} (${ts})`;
            
            // role_id 3 is Learner based on earlier check
            const userRes = await client.query(`
                INSERT INTO users (user_code, role_id, email, password_hash, full_name, is_active)
                VALUES ($1, 3, $2, 'hashed_password', $3, true)
                RETURNING user_id;
            `, [code, email, name]);
            
            const uId = userRes.rows[0].user_id;
            learnerIds.push(uId);

            // profile
            await client.query(`
                INSERT INTO learner_profiles (user_id, current_streak, longest_streak, total_practice_seconds, total_points, total_stars)
                VALUES ($1, 0, 0, 0, 0, 0);
            `, [uId]);

            // link to lessons
            await client.query(`
                INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score)
                VALUES ($1, $2, 'IN_PROGRESS', 0, 0);
            `, [uId, lesson1Id]);

            await client.query(`
                INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score)
                VALUES ($1, $2, 'LOCKED', 0, 0);
            `, [uId, lesson2Id]);
        }

        console.log("Created 5 learners and enrolled them:", learnerIds);
        
        console.log("Done!");
    } catch (err) {
        console.error("Error", err);
    } finally {
        await client.end();
    }
}

run();
