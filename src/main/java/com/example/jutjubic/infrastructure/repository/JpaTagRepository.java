package com.example.jutjubic.infrastructure.repository;

import com.example.jutjubic.infrastructure.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTagRepository extends JpaRepository<TagEntity, Long> {
    TagEntity findByName(String name);
}
