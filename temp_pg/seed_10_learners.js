const { Client } = require('pg');
const crypto = require('crypto');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        
        // 1. Get instructor user_id
        const resInstructor = await client.query(`SELECT user_id FROM public.users WHERE email = 'dantranh.master@vietstage.com'`);
        if (resInstructor.rows.length === 0) {
            console.log("Instructor not found!");
            return;
        }
        const instructorId = resInstructor.rows[0].user_id;
        
        // 2. Get instructor's lessons
        const resLessons = await client.query(`SELECT lesson_id FROM lessons WHERE created_by_user_id = $1`, [instructorId]);
        if (resLessons.rows.length === 0) {
            console.log("Instructor has no lessons!");
            return;
        }
        const lessonIds = resLessons.rows.map(row => row.lesson_id);
        console.log(`Instructor has lessons: ${lessonIds.join(', ')}`);
        
        // 3. Create 10 learners
        const passwordHash = crypto.createHash('sha256').update('password123').digest('hex');
        let newLearnerIds = [];
        
        for (let i = 1; i <= 10; i++) {
            const userCode = `LNR_MOCK_${Date.now()}_${i}`;
            const email = `mock.learner${Date.now()}_${i}@vietstage.com`;
            
            const resUser = await client.query(`
                INSERT INTO public.users (user_code, email, password_hash, full_name, role_id, is_active)
                VALUES ($1, $2, $3, $4, (SELECT role_id FROM roles WHERE role_name = 'LEARNER'), true)
                RETURNING user_id
            `, [userCode, email, passwordHash, `Mock Learner ${i}`]);
            
            const learnerId = resUser.rows[0].user_id;
            newLearnerIds.push(learnerId);
            
            // create learner_profiles
            await client.query(`
                INSERT INTO learner_profiles (user_id, total_practice_seconds)
                VALUES ($1, 0)
            `, [learnerId]);
        }
        
        console.log(`Created 10 learners: ${newLearnerIds.join(', ')}`);
        
        // 4. Enroll them in instructor's lessons
        for (const learnerId of newLearnerIds) {
            for (const lessonId of lessonIds) {
                await client.query(`
                    INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, best_score)
                    VALUES ($1, $2, 'IN_PROGRESS', 85.5)
                `, [learnerId, lessonId]);
            }
        }
        
        console.log(`Enrolled learners in lessons!`);
        
    } catch (err) {
        console.error('Error:', err);
    } finally {
        await client.end();
    }
}
run();
