package com.example.jutjubic.infrastructure.repository;

import com.example.jutjubic.infrastructure.entity.VideoViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaVideoViewRepository extends JpaRepository<VideoViewEntity, Long> {

    /**
     * Pronalazi sve preglede videa u zadatom vremenskom periodu
     */
    @Query("SELECT v FROM VideoViewEntity v WHERE v.viewedAt >= :startDate")
    List<VideoViewEntity> findViewsSince(@Param("startDate") LocalDateTime startDate);

    /**
     * Broji preglede za svaki video u poslednjih N dana
     * Vraća listu: [videoId, day_diff, view_count]
     */
    @Query(value = """
        SELECT 
            vv.video_id,
            (CURRENT_DATE - DATE(vv.viewed_at)) as day_diff,
            COUNT(*) as view_count
        FROM video_views vv
        WHERE vv.viewed_at >= :startDate
        GROUP BY vv.video_id, day_diff
        ORDER BY vv.video_id, day_diff
        """, nativeQuery = true)
    List<Object[]> countViewsByVideoAndDay(@Param("startDate") LocalDateTime startDate);
}
