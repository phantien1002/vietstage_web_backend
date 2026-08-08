const fs = require('fs');
let sql = '\n-- MOCK DATA FOR METRICS (Dashboard)\n';

// 1. Usage Sessions for last 3 months
sql += 'INSERT INTO usage_sessions (user_id, session_token, started_at, last_active_at, ip_address, user_agent, is_active) VALUES\n';
for(let i=1; i<=10; i++) {
  let date = new Date(Date.now() - i * 86400000);
  sql += `(3, 'tok_${i}', '${date.toISOString()}', '${date.toISOString()}', '192.168.1.1', 'Mozilla', true)${i===10 ? ';' : ','}\n`;
}

// 2. Practice Sessions (for duration and retention)
sql += '\nINSERT INTO practice_sessions (learner_id, started_at, ended_at, duration_minutes) VALUES\n';
for(let i=1; i<=10; i++) {
  let date = new Date(Date.now() - i * 86400000);
  sql += `(3, '${date.toISOString()}', '${date.toISOString()}', ${10 + i})${i===10 ? ';' : ','}\n`;
}

// 3. Practice Attempts (for popular instruments)
sql += '\nINSERT INTO practice_attempts (session_id, learner_id, exercise_id, pitch_score, rhythm_score, dynamics_score, breath_score, tonal_quality_score, total_score, stars, points_earned, sync_status) VALUES\n';
for(let i=1; i<=10; i++) {
  sql += `(${i}, 3, 1, 80, 80, 80, 80, 80, 80, 3, 10, 'SYNCED')${i===10 ? ';' : ','}\n`;
}

fs.appendFileSync('vietstage_full_reset_v2.sql', sql);
console.log('Appended mock metrics data to sql file');
