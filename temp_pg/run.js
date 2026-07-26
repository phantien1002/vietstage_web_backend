const { Client } = require('pg');
const client = new Client({ 
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require',
    ssl: { rejectUnauthorized: false }
}); 

client.connect().then(() => 
    client.query(`
        INSERT INTO techniques (instrument_id, name, description, guide_url) 
        VALUES 
            ((SELECT instrument_id FROM instruments WHERE name = 'Dan Tranh'), 'Rung', 'Ky thuat rung day co ban', 'http://example.com/rung'), 
            ((SELECT instrument_id FROM instruments WHERE name = 'Dan Tranh'), 'Mo', 'Ky thuat mo day', 'http://example.com/mo'), 
            ((SELECT instrument_id FROM instruments WHERE name = 'Sao Truc'), 'Vuot', 'Ky thuat vuot nhip', 'http://example.com/vuot') 
        ON CONFLICT DO NOTHING;
    `)
).then(() => { 
    console.log('Seed techniques successfully!'); 
    process.exit(0); 
}).catch(e => {
    console.error(e);
    process.exit(1);
});
