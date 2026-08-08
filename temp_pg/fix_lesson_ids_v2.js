const { Client } = require('pg');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        
        console.log("Fixing lesson IDs dynamically...");
        
        const res = await client.query('SELECT lesson_id FROM lessons WHERE lesson_id > 3 ORDER BY lesson_id;');
        if (res.rows.length >= 2) {
            const id1 = res.rows[0].lesson_id;
            const id2 = res.rows[1].lesson_id;
            
            await client.query("DELETE FROM learner_lesson_progress WHERE lesson_id IN ($1, $2);", [id1, id2]);
            await client.query("UPDATE lessons SET lesson_id = 4 WHERE lesson_id = $1;", [id1]);
            await client.query("UPDATE lessons SET lesson_id = 5 WHERE lesson_id = $1;", [id2]);
            
            await client.query("SELECT setval('lessons_lesson_id_seq', 5, true);");
            
            const learnerIds = ['13', '14', '15', '16', '17'];
            for (const uId of learnerIds) {
                await client.query(`
                    INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score)
                    VALUES ($1, 4, 'COMPLETED', 2, 80)
                    ON CONFLICT DO NOTHING;
                `, [uId]);
                
                await client.query(`
                    INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score)
                    VALUES ($1, 5, 'IN_PROGRESS', 0, 0)
                    ON CONFLICT DO NOTHING;
                `, [uId]);
            }
            console.log("IDs fixed sequentially!");
        } else {
            console.log("Not enough lessons to fix.");
        }
    } catch (err) {
        console.error("Error", err);
    } finally {
        await client.end();
    }
}

run();
