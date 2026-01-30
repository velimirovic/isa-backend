package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.api.dto.map.VideoMarkerDTO;
import com.example.jutjubic.core.domain.FilterType;
import com.example.jutjubic.core.service.VideoMapService;
import com.example.jutjubic.core.domain.ZoomLevel;
import com.example.jutjubic.infrastructure.entity.VideoPostEntity;
import com.example.jutjubic.infrastructure.repository.JpaVideoPostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class VideoMapServiceImpl implements VideoMapService {

    private final JpaVideoPostRepository videoPostRepository;

    public VideoMapServiceImpl(JpaVideoPostRepository videoPostRepository) {
        this.videoPostRepository = videoPostRepository;
    }

    @Override
    public List<VideoMarkerDTO> getVideosInBounds(double minLat, double maxLat,
                                                  double minLng, double maxLng) {
        List<VideoPostEntity> videos = videoPostRepository.findPublishedWithLocationInBounds(minLat, maxLat, minLng, maxLng);

        return videos.stream()
                .map(this::mapToMarkerDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "mapTiles", key = "#zoom + '_' + #minTileX + '_' + #maxTileX + '_' + #minTileY + '_' + #maxTileY + '_' + #filter")
    public List<VideoMarkerDTO> getVideosForTiles(int zoom, int minTileX, int maxTileX, int minTileY, int maxTileY, FilterType filter) {
        // Konvertuj tile koordinate u geografske koordinate (lat/lng)
        double minLat = tile2lat(maxTileY + 1, zoom); // Y ide obrnuto
        double maxLat = tile2lat(minTileY, zoom);
        double minLng = tile2lon(minTileX, zoom);
        double maxLng = tile2lon(maxTileX + 1, zoom);

        // Uzmi zoom nivo strategiju
        ZoomLevel zoomLevel = ZoomLevel.fromZoom(zoom);
        int maxVideosPerTile = zoomLevel.getMaxVideosPerTile();

        // Izračunaj koliko tile-ova pokrivamo
        int totalTiles = (maxTileX - minTileX + 1) * (maxTileY - minTileY + 1);
        int maxTotalVideos = totalTiles * maxVideosPerTile;

        // Pozovi repository sa limitom
        return getVideosInBoundsWithLimit(minLat, maxLat, minLng, maxLng, maxTotalVideos, filter);
    }

    //Metoda sa limitom
    public List<VideoMarkerDTO> getVideosInBoundsWithLimit(double minLat, double maxLat,
                                                           double minLng, double maxLng,
                                                           int limit, FilterType filter) {

        LocalDateTime now = LocalDateTime.now();
        List<VideoPostEntity> videos = switch (filter) {
            case LAST_30_DAYS -> {
                LocalDateTime from = now.minusDays(30);
                yield videoPostRepository.findPublishedWithLocationInBoundsWithLimitByCreatedAtGreaterThanEqual(
                        from, minLat, maxLat, minLng, maxLng, limit);
            }
            case CURRENT_YEAR -> {
                LocalDateTime from = LocalDate.now()
                        .withDayOfYear(1)
                        .atStartOfDay();
                yield videoPostRepository.findPublishedWithLocationInBoundsWithLimitByCreatedAtGreaterThanEqual(
                        from, minLat, maxLat, minLng, maxLng, limit
                );
            }
            default -> videoPostRepository.findPublishedWithLocationInBoundsWithLimit(
                    minLat, maxLat, minLng, maxLng, limit
            );
        };

        return videos.stream()
                .map(this::mapToMarkerDTO)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "mapTiles", allEntries = true)
    public void refreshAllTileCache() {
        log.info("refreshing all tile cache, will be filled on next user request");
    }

    // Tile matematika: tile broj -> geografska koordinata
    private double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180.0;
    }

    private double tile2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    private VideoMarkerDTO mapToMarkerDTO(VideoPostEntity video) {
        return new VideoMarkerDTO(
                video.getId(),
                video.getTitle(),
                video.getDraftId(),
                video.getLatitude(),
                video.getLongitude(),
                video.getThumbnailPath(),
                video.getViewCount(),
                video.getAuthor().getUsername()
        );
    }
}