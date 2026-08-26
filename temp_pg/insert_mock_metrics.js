const { Client } = require('pg');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        console.log('Connected. Inserting mock metrics data into existing DB...');

        // 1. Insert Usage Sessions
        let usageSql = 'INSERT INTO usage_sessions (user_id, platform, started_at, ended_at) VALUES ';
        const usageVals = [];
        for(let i=1; i<=10; i++) {
            let date = new Date(Date.now() - i * 86400000);
            let end_date = new Date(date.getTime() + (10 + i) * 60000);
            usageVals.push(`(3, 'WEB', '${date.toISOString()}', '${end_date.toISOString()}')`);
        }
        await client.query(usageSql + usageVals.join(', '));
        console.log('Inserted usage_sessions');

        // 2. Insert Practice Sessions
        let sessionSql = 'INSERT INTO practice_sessions (learner_id, started_at, ended_at, duration_minutes) VALUES ';
        const sessionVals = [];
        for(let i=1; i<=10; i++) {
            let date = new Date(Date.now() - i * 86400000);
            let end_date = new Date(date.getTime() + (10 + i) * 60000);
            sessionVals.push(`(3, '${date.toISOString()}', '${end_date.toISOString()}', ${10 + i})`);
        }
        await client.query(sessionSql + sessionVals.join(', ') + ' RETURNING id');
        console.log('Inserted practice_sessions');
        
        // 3. Insert Practice Attempts
        let attemptSql = 'INSERT INTO practice_attempts (learner_id, exercise_id, started_at, completed_at, pitch_score, rhythm_score, tonal_quality_score, breath_score, dynamics_score, total_score, session_id) VALUES ';
        const attemptVals = [];
        for(let i=1; i<=10; i++) {
            let date = new Date(Date.now() - i * 86400000);
            let end_date = new Date(date.getTime() + 300000);
            // using exercise_id = 1 (assuming it exists), session_id = 1
            attemptVals.push(`(3, 1, '${date.toISOString()}', '${end_date.toISOString()}', 80, 80, 80, 80, 80, 80, 1)`);
        }
        await client.query(attemptSql + attemptVals.join(', '));
        console.log('Inserted practice_attempts');
        
        console.log('Successfully inserted all mock data!');
    } catch (err) {
        console.error('Error:', err);
    } finally {
        await client.end();
    }
}
run();
