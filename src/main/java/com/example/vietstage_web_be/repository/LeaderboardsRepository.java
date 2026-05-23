package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Leaderboards;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardsRepository extends JpaRepository<Leaderboards, Long> {

}