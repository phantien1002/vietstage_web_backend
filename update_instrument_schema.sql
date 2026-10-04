ALTER TABLE quizzes ADD COLUMN IF NOT EXISTS instrument_id BIGINT;
ALTER TABLE quizzes ALTER COLUMN lesson_id DROP NOT NULL;
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_quizzes_instrument') THEN 
        ALTER TABLE quizzes ADD CONSTRAINT fk_quizzes_instrument FOREIGN KEY (instrument_id) REFERENCES instruments(id); 
    END IF; 
END $$; 

ALTER TABLE minigame_challenges ADD COLUMN IF NOT EXISTS instrument_id BIGINT;
ALTER TABLE minigame_challenges ALTER COLUMN lesson_id DROP NOT NULL;
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_minigames_instrument') THEN 
        ALTER TABLE minigame_challenges ADD CONSTRAINT fk_minigames_instrument FOREIGN KEY (instrument_id) REFERENCES instruments(id); 
    END IF; 
END $$;
