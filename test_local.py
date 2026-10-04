import psycopg2
import requests

db_url = 'postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres'
conn = psycopg2.connect(db_url)
cur = conn.cursor()

# Get a learner email
cur.execute("SELECT email FROM users WHERE role_id = (SELECT role_id FROM roles WHERE role_name='LEARNER') LIMIT 1")
email = cur.fetchone()[0]
print(f"Testing with learner email: {email}")

# Login
r2 = requests.post('http://localhost:9191/api/auth/login', json={'email': email, 'password': '123'})
if not r2.json().get('data'):
    r2 = requests.post('http://localhost:9191/api/auth/login', json={'email': email, 'password': 'password'})

token = r2.json().get('data', {}).get('accessToken')
if not token:
    print("Could not login. Response:", r2.text)
    exit(1)

headers = {'Authorization': 'Bearer ' + token}
r3 = requests.get('http://localhost:9191/api/app/courses/progress', headers=headers)
print("Status Code:", r3.status_code)
if r3.status_code == 200:
    data = r3.json()
    lessons = data.get('data', {}).get('lessons', [])
    print(f"Successfully fetched {len(lessons)} lessons in progress.")
else:
    print("Failed to fetch progress:", r3.text)
