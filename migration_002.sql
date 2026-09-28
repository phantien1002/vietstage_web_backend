UPDATE learner_profiles 
SET has_full_access = TRUE 
WHERE user_id = (SELECT user_id FROM users WHERE email = 'phuclong2710@gmail.com');
