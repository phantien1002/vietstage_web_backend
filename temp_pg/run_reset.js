const { Client } = require('pg');
const fs = require('fs');
const path = require('path');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        
        console.log("Connected to DB. Running full reset script...");
        const sql = fs.readFileSync(path.join(__dirname, '..', 'vietstage_full_reset_v2.sql'), 'utf8');
        await client.query(sql);

        console.log("Database reset and updated successfully.");
    } catch (err) {
        console.error("Error executing query", err);
    } finally {
        await client.end();
    }
}

run();
