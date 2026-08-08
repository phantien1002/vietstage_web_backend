const { Client } = require('pg');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        
        console.log("Checking DB...");
        
        const inst = await client.query('SELECT id, name FROM instruments;');
        console.log('Instruments:', inst.rows);
        
        const roles = await client.query('SELECT role_id, role_name FROM roles;');
        console.log('Roles:', roles.rows);
        
        const instructor = await client.query("SELECT user_id, email FROM users WHERE email='dantranh.master@vietstage.com';");
        console.log('Instructor:', instructor.rows);
        
        const skills = await client.query('SELECT skill_level_id, name FROM skill_levels;');
        console.log('Skills:', skills.rows);
        
    } catch (err) {
        console.error("Error", err);
    } finally {
        await client.end();
    }
}

run();
