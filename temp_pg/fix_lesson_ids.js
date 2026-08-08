const { Client } = require('pg');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        
        console.log("Fixing lesson IDs...");
        
        // 1. Delete progress for 16 and 17 so we can rename them safely
        await client.query("DELETE FROM learner_lesson_progress WHERE lesson_id IN (16, 17);");
        
        // 2. Rename IDs
        await client.query("UPDATE lessons SET lesson_id = 4 WHERE lesson_id = 16;");
        await client.query("UPDATE lessons SET lesson_id = 5 WHERE lesson_id = 17;");
        
        // 3. Reset sequence so next insert gets 6
        await client.query("SELECT setval('lessons_lesson_id_seq', 5, true);");
        
        // 4. Re-enroll learners into 4 and 5
        const learnerIds = ['13', '14', '15', '16', '17'];
        for (const uId of learnerIds) {
            // Enroll in Lesson 4 (Sáo 1)
            await client.query(`
                INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score)
                VALUES ($1, 4, 'COMPLETED', 2, 80);
            `, [uId]);
            
            // Enroll in Lesson 5 (Sáo 2)
            await client.query(`
                INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score)
                VALUES ($1, 5, 'IN_PROGRESS', 0, 0);
            `, [uId]);
        }
        
        console.log("IDs fixed sequentially!");
    } catch (err) {
        console.error("Error", err);
    } finally {
        await client.end();
    }
}

run();
