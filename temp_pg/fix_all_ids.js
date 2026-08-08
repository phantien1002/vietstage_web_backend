const { Client } = require('pg');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

const renameTasks = [
    { table: 'content_reviews', oldCol: 'review_id', newCol: 'id' },
    { table: 'techniques', oldCol: 'technique_id', newCol: 'id' },
    { table: 'exercises', oldCol: 'exercise_id', newCol: 'id' },
    { table: 'practice_attempts', oldCol: 'attempt_id', newCol: 'id' },
    { table: 'instructor_feedback', oldCol: 'feedback_id', newCol: 'id' },
    { table: 'quizzes', oldCol: 'quiz_id', newCol: 'id' },
    { table: 'quiz_questions', oldCol: 'question_id', newCol: 'id' },
    { table: 'quiz_options', oldCol: 'option_id', newCol: 'id' },
    { table: 'quiz_attempts', oldCol: 'attempt_id', newCol: 'id' },
    { table: 'quiz_answers', oldCol: 'answer_id', newCol: 'id' },
    { table: 'minigame_challenges', oldCol: 'challenge_id', newCol: 'id' },
    { table: 'minigame_attempts', oldCol: 'attempt_id', newCol: 'id' },
    { table: 'achievements', oldCol: 'achievement_id', newCol: 'id' },
    { table: 'cosmetic_items', oldCol: 'cosmetic_item_id', newCol: 'id' },
    { table: 'daily_challenges', oldCol: 'challenge_id', newCol: 'id' },
    { table: 'app_configs', oldCol: 'config_id', newCol: 'id' }
];

const compositeTasks = [
    { table: 'learner_achievements' },
    { table: 'learner_cosmetics' },
    { table: 'learner_daily_challenges' },
    { table: 'learner_lesson_progress' },
    { table: 'instructor_instruments' },
    { table: 'learner_instruments' }
];

async function run() {
    try {
        await client.connect();
        
        console.log("Renaming simple primary keys...");
        for (const task of renameTasks) {
            try {
                await client.query(`ALTER TABLE ${task.table} RENAME COLUMN ${task.oldCol} TO ${task.newCol};`);
                console.log(`Renamed in ${task.table}`);
            } catch (e) {
                console.log(`Skip or failed for ${task.table}: ${e.message}`);
            }
        }

        console.log("Fixing composite primary keys to use 'id'...");
        for (const task of compositeTasks) {
            try {
                await client.query(`ALTER TABLE ${task.table} DROP CONSTRAINT ${task.table}_pkey CASCADE;`);
                await client.query(`ALTER TABLE ${task.table} ADD COLUMN id BIGSERIAL PRIMARY KEY;`);
                console.log(`Fixed composite PK for ${task.table}`);
            } catch (e) {
                console.log(`Skip or failed for ${task.table}: ${e.message}`);
            }
        }
        
        console.log("Done");
    } catch (err) {
        console.error("Fatal error", err);
    } finally {
        await client.end();
    }
}

run();
