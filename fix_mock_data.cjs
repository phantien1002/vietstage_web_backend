const fs = require('fs'); let sql = fs.readFileSync('vietstage_full_reset_v2.sql', 'utf8');
const lines = sql.split('\n');
const filtered = [];
let skip = false;
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('-- MOCK DATA FOR METRICS')) { skip = true; break; }
  filtered.push(lines[i]);
}
sql = filtered.join('\n');

sql += '\n-- MOCK DATA FOR METRICS (Dashboard)\n';
// usage_sessions: usage_session_id(auto), user_id, platform, started_at, ended_at, created_at
sql += 'INSERT INTO usage_sessions (user_id, platform, started_at, ended_at) VALUES\n';
for(let i=1; i<=10; i++) {
  let date = new Date(Date.now() - i * 86400000);
  let end_date = new Date(date.getTime() + (10 + i) * 60000);
  sql += `(3, 'WEB', '${date.toISOString()}', '${end_date.toISOString()}')${i===10 ? ';' : ','}\n`;
}

// practice_attempts: id(auto), client_uuid(auto), learner_user_id, exercise_id, started_at, completed_at, pitch_score, rhythm_score, tonal_quality_score, breath_score, dynamics_score, composite_score
sql += '\nINSERT INTO practice_attempts (learner_user_id, exercise_id, started_at, completed_at, pitch_score, rhythm_score, tonal_quality_score, breath_score, dynamics_score, composite_score) VALUES\n';
for(let i=1; i<=10; i++) {
  let date = new Date(Date.now() - i * 86400000);
  let end_date = new Date(date.getTime() + 300000);
  sql += `(3, 1, '${date.toISOString()}', '${end_date.toISOString()}', 80, 80, 80, 80, 80, 80)${i===10 ? ';' : ','}\n`;
}

fs.writeFileSync('vietstage_full_reset_v2.sql', sql);
console.log('Fixed mock data');
