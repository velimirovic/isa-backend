package com.example.jutjubic.infrastructure.repository;

import com.example.jutjubic.infrastructure.entity.UserEntity;
import com.example.jutjubic.infrastructure.entity.VideoPostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaVideoPostRepository extends JpaRepository<VideoPostEntity, Long> {

    @Query("""
        SELECT v
        FROM VideoPostEntity v
        WHERE v.status = 'PUBLISHED'
        ORDER BY v.createdAt DESC
    """)
    Page<VideoPostEntity> findAllPublished(Pageable pageable);
    Page<VideoPostEntity> findAllByAuthor(UserEntity author, Pageable pageable);
    VideoPostEntity findById(long id);
    VideoPostEntity findByDraftId(String draftId);

    @Query("""
        SELECT v
        FROM VideoPostEntity v
        WHERE v.status = 'DRAFT' and v.author = :author
    """)
    VideoPostEntity findDraftByAuthor(UserEntity author);
}
