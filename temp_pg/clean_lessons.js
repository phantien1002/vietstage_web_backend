const { Client } = require('pg');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        
        console.log("Cleaning up old garbage lessons...");
        await client.query("DELETE FROM learner_lesson_progress WHERE lesson_id > 3;");
        await client.query("DELETE FROM lessons WHERE lesson_id > 3;");
        
        console.log("Renaming existing lessons for logical curriculum...");
        await client.query("UPDATE lessons SET title = 'Bài 1: Làm quen Đàn Tranh' WHERE lesson_id = 1;");
        await client.query("UPDATE lessons SET title = 'Bài 2: Các nốt nhạc cơ bản' WHERE lesson_id = 2;");
        
        console.log("Creating new logical lessons for Sáo trúc under dantranh.master...");
        
        // instructor = 3, Sáo = 3, skill = 1
        const lesson3Res = await client.query(`
            INSERT INTO lessons (lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, status, order_index)
            VALUES ('LESSON_SAO_10', 3, 1, 3, 'Bài 1: Làm quen Sáo Trúc', 'Cách cầm sáo và làm quen với sáo trúc', 'APPROVED', 10)
            RETURNING lesson_id;
        `);
        const l3 = lesson3Res.rows[0].lesson_id;

        const lesson4Res = await client.query(`
            INSERT INTO lessons (lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, status, order_index)
            VALUES ('LESSON_SAO_11', 3, 1, 3, 'Bài 2: Kỹ thuật lấy hơi và thổi', 'Học cách lấy hơi dài và thổi ra tiếng sáo chuẩn', 'APPROVED', 11)
            RETURNING lesson_id;
        `);
        const l4 = lesson4Res.rows[0].lesson_id;
        
        console.log("Enrolling 5 learners into the 4 lessons...");
        const learnerIds = ['13', '14', '15', '16', '17'];
        
        // delete old progress for these learners
        for (const uId of learnerIds) {
            await client.query("DELETE FROM learner_lesson_progress WHERE learner_user_id = $1;", [uId]);
        }
        
        for (const uId of learnerIds) {
            // Enroll in Lesson 1 (Đàn Tranh 1)
            await client.query(`
                INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score)
                VALUES ($1, 1, 'COMPLETED', 3, 95);
            `, [uId]);
            
            // Enroll in Lesson 2 (Đàn Tranh 2)
            await client.query(`
                INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score)
                VALUES ($1, 2, 'IN_PROGRESS', 0, 0);
            `, [uId]);
            
            // Enroll in Lesson 3 (Sáo 1)
            await client.query(`
                INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score)
                VALUES ($1, $2, 'COMPLETED', 2, 80);
            `, [uId, l3]);
            
            // Enroll in Lesson 4 (Sáo 2)
            await client.query(`
                INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score)
                VALUES ($1, $2, 'IN_PROGRESS', 0, 0);
            `, [uId, l4]);
        }

        console.log("Curriculum standardized!");
    } catch (err) {
        console.error("Error", err);
    } finally {
        await client.end();
    }
}

run();
