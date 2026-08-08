import os

files = [
    'vietstage_full_reset_v2.sql',
    'vietstage_full_reset.sql',
    'vietstage.sql'
]

for file in files:
    if os.path.exists(file):
        with open(file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Replace the primary key in CREATE TABLE instruments
        content = content.replace(
            "instrument_id  BIGSERIAL PRIMARY KEY,",
            "id             BIGSERIAL PRIMARY KEY,\n    instrument_code VARCHAR(50) UNIQUE,"
        )
        content = content.replace(
            "instrument_id        BIGSERIAL PRIMARY KEY,",
            "id             BIGSERIAL PRIMARY KEY,\n    instrument_code VARCHAR(50) UNIQUE,"
        )
        content = content.replace(
            "instrument_id BIGSERIAL PRIMARY KEY,",
            "id BIGSERIAL PRIMARY KEY,\n    instrument_code VARCHAR(50) UNIQUE,"
        )

        # Replace all references
        content = content.replace(
            "REFERENCES instruments(instrument_id)",
            "REFERENCES instruments(id)"
        )
        
        with open(file, 'w', encoding='utf-8') as f:
            f.write(content)

print("Updated SQL files successfully.")
