ALTER TABLE learner_profiles ADD COLUMN has_full_access BOOLEAN DEFAULT FALSE;
UPDATE learner_profiles SET has_full_access = TRUE WHERE user_id = (SELECT user_id FROM users WHERE email = 'thanhdattb19@gmail.com');
