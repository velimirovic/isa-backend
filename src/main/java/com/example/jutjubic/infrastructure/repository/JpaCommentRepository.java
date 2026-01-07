package com.example.jutjubic.infrastructure.repository;

import com.example.jutjubic.infrastructure.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


//Repository za komentare
// Velika količina komentara treba da se vrati kroz paginaciju
//Komentari se prikazuju od najnovijeg do najstarijeg

@Repository
public interface JpaCommentRepository extends JpaRepository<CommentEntity, Long> {

    //Pronalazi komentare za određenu video objavu sa paginacijom
    //Sortira po vremenu kreiranja (najnoviji prvi)

    @Query("""
        SELECT c
        FROM CommentEntity c
        WHERE c.video.id = :videoId
        ORDER BY c.createdAt DESC
    """)
    Page<CommentEntity> findByVideoIdOrderByCreatedAtDesc(
            @Param("videoId") Long videoId,
            Pageable pageable
    );


    //Broji ukupan broj komentara za video objavu

    @Query("""
        SELECT COUNT(c)
        FROM CommentEntity c
        WHERE c.video.id = :videoId
    """)
    Long countByVideoId(@Param("videoId") Long videoId);
}