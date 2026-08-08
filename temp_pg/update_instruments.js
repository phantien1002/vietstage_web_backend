const { Client } = require('pg');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        
        console.log("Connected to DB. Starting updates...");

        // Delete references first
        await client.query(`DELETE FROM learner_instruments WHERE instrument_id NOT IN (1, 2, 3, 4)`);
        await client.query(`DELETE FROM instructor_instruments WHERE instrument_id NOT IN (1, 2, 3, 4)`);
        await client.query(`DELETE FROM exercises WHERE lesson_id IN (SELECT lesson_id FROM lessons WHERE instrument_id NOT IN (1, 2, 3, 4))`);
        await client.query(`DELETE FROM exercises WHERE instrument_id NOT IN (1, 2, 3, 4)`);
        await client.query(`DELETE FROM content_reviews WHERE lesson_id IN (SELECT lesson_id FROM lessons WHERE instrument_id NOT IN (1, 2, 3, 4))`);
        await client.query(`DELETE FROM lessons WHERE instrument_id NOT IN (1, 2, 3, 4)`);

        // Delete the extra ones the user added
        const res = await client.query(`DELETE FROM instruments WHERE instrument_id NOT IN (1, 2, 3, 4)`);
        console.log(`Deleted ${res.rowCount} extra instruments.`);

        // Update existing ones (assuming id 1,2,3,4 are the originals)
        await client.query(`UPDATE instruments SET name = 'Đàn Tranh' WHERE instrument_id = 1`);
        await client.query(`UPDATE instruments SET name = 'Đàn Bầu' WHERE instrument_id = 2`);
        await client.query(`UPDATE instruments SET name = 'Sáo' WHERE instrument_id = 3`);
        await client.query(`UPDATE instruments SET name = 'Trống Chầu' WHERE instrument_id = 4`);

        console.log("Database instruments updated successfully.");
    } catch (err) {
        console.error("Error executing query", err.stack);
    } finally {
        await client.end();
    }
}

run();
