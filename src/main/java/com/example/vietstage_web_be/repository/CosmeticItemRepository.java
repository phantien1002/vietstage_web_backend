package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.CosmeticItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CosmeticItemRepository extends JpaRepository<CosmeticItem, Long> {
    List<CosmeticItem> findByItemType(String itemType);
}