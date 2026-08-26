const { Client } = require('pg');

const client = new Client({
    connectionString: "postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres",
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        await client.query("ALTER TABLE learner_profiles ADD COLUMN IF NOT EXISTS spendable_stars INTEGER DEFAULT 0");
        console.log("Column added");
    } catch (e) {
        console.error(e);
    } finally {
        await client.end();
    }
}
run();
