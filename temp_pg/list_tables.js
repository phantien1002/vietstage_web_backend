const { Client } = require('pg');
const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

client.connect()
    .then(() => client.query("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;"))
    .then(res => {
        console.log('Total:', res.rows.length);
        res.rows.forEach(r => console.log(r.table_name));
        return client.end();
    })
    .catch(e => console.error(e));
