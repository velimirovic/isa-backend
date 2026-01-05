package com.example.jutjubic.infrastructure.persistence.repository;

import com.example.jutjubic.infrastructure.persistence.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTagRepository extends JpaRepository<TagEntity, Long> {
    TagEntity findByName(String name);
}
