package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.InstructorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstructorProfileRepository extends JpaRepository<InstructorProfile, Long> {

}
