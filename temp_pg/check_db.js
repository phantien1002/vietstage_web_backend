const { Client } = require('pg');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        
        console.log("Connected to DB. Checking tables...");
        const res = await client.query(`SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;`);
        console.log("Tables in public schema:", res.rows.map(r => r.table_name));

        const res2 = await client.query(`SELECT * FROM instruments;`);
        console.log("Instruments data:", res2.rows);

    } catch (err) {
        console.error("Error executing query", err);
    } finally {
        await client.end();
    }
}

run();
