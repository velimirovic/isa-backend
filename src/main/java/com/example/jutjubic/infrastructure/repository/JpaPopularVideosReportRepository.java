package com.example.jutjubic.infrastructure.repository;

import com.example.jutjubic.infrastructure.entity.PopularVideosReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaPopularVideosReportRepository extends JpaRepository<PopularVideosReportEntity, Long> {

    /**
     * Pronalazi top 3 videa iz poslednjeg izvršenog ETL pipeline-a
     */
    @Query("""
        SELECT p FROM PopularVideosReportEntity p 
        WHERE p.runDate = (
            SELECT MAX(p2.runDate) FROM PopularVideosReportEntity p2
        )
        ORDER BY p.rank ASC
        """)
    List<PopularVideosReportEntity> findLatestTopVideos();

    /**
     * Briše stare izveštaje (opciono - može da se koristi za cleanup)
     */
    @Query("DELETE FROM PopularVideosReportEntity p WHERE p.runDate < :cutoffDate")
    void deleteOldReports(@org.springframework.data.repository.query.Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
