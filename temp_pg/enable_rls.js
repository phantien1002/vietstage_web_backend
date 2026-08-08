const { Client } = require('pg');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        
        console.log("Connected to DB. Enabling RLS for all tables...");
        const res = await client.query(`SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE';`);
        
        for (const row of res.rows) {
            const tableName = row.table_name;
            await client.query(`ALTER TABLE "${tableName}" ENABLE ROW LEVEL SECURITY;`);
            console.log(`Enabled RLS for table: ${tableName}`);
        }

        console.log("Successfully enabled RLS on all tables! The UNRESTRICTED badges will now disappear.");
    } catch (err) {
        console.error("Error executing query", err);
    } finally {
        await client.end();
    }
}

run();
