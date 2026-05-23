package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.UserProfiles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfilesRepository extends JpaRepository<UserProfiles, Long> {

}