package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.InstructorProfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstructorsRepository extends JpaRepository<InstructorProfiles, Long> {

}
