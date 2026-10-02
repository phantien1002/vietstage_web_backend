import re

filepath = 'src/main/resources/application.properties'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('spring.jpa.hibernate.ddl-auto=update', 'spring.jpa.hibernate.ddl-auto=none')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated ddl-auto to none")
