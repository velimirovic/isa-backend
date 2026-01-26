package com.example.jutjubic.infrastructure.repository;

import com.example.jutjubic.infrastructure.entity.UserEntity;
import com.example.jutjubic.infrastructure.entity.VideoPostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

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

    @Modifying
    @Query("""
        UPDATE VideoPostEntity v
        SET v.viewCount = v.viewCount + 1
        WHERE v.id = :id
    """)
    void incrementViewCount(@Param("id") Long id);

    @Query("""
        SELECT v FROM VideoPostEntity v 
        WHERE v.status = 'PUBLISHED' 
        AND v.latitude IS NOT NULL 
        AND v.longitude IS NOT NULL 
        AND v.latitude BETWEEN :minLat AND :maxLat 
        AND v.longitude BETWEEN :minLng AND :maxLng
    """)
    List<VideoPostEntity> findPublishedWithLocationInBounds(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng
    );

    @Query("SELECT v FROM VideoPostEntity v WHERE v.author.username = :username AND v.status = 'PUBLISHED'")
    Page<VideoPostEntity> findByAuthorUsernameAndPublished(@Param("username") String username, Pageable pageable);
           
    @Query(value = """
    SELECT v.* FROM videos v 
    WHERE v.status = 'PUBLISHED' 
    AND v.latitude IS NOT NULL 
    AND v.longitude IS NOT NULL 
    AND v.latitude BETWEEN :minLat AND :maxLat 
    AND v.longitude BETWEEN :minLng AND :maxLng
    ORDER BY v.view_count DESC, v.created_at DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<VideoPostEntity> findPublishedWithLocationInBoundsWithLimit(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng,
            @Param("limit") int limit
    );
}
