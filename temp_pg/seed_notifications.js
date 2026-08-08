const { Client } = require('pg');

const client = new Client({
    connectionString: 'postgres://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres',
    ssl: { rejectUnauthorized: false }
});

async function run() {
    try {
        await client.connect();
        
        console.log("Seeding mock notifications...");
        
        const notifications = [
            { userId: 3, title: 'Chào mừng trở lại!', message: 'Chúc bạn một ngày giảng dạy hiệu quả!', type: 'SYSTEM' },
            { userId: 3, title: 'Học viên nộp bài', message: 'Học viên 1 vừa nộp bài thực hành Đàn Tranh.', type: 'REMINDER' },
            { userId: 13, title: 'Bạn đã đạt mốc mới!', message: 'Bạn đã hoàn thành 5 ngày học liên tiếp!', type: 'ACHIEVEMENT' },
            { userId: 13, title: 'Bài tập mới', message: 'Giảng viên vừa thêm bài tập Sáo trúc mới.', type: 'SYSTEM' },
            { userId: 14, title: 'Nhắc nhở học tập', message: 'Đã 2 ngày rồi bạn chưa vào học, hãy tiếp tục nhé!', type: 'REMINDER' }
        ];

        for (const noti of notifications) {
            await client.query(`
                INSERT INTO notifications (user_id, title, message, type, is_read, created_at)
                VALUES ($1, $2, $3, $4, false, NOW())
            `, [noti.userId, noti.title, noti.message, noti.type]);
        }
        
        console.log("Seeded mock notifications successfully!");
    } catch (err) {
        console.error("Error", err);
    } finally {
        await client.end();
    }
}

run();
