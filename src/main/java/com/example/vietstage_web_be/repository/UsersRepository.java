package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {

}