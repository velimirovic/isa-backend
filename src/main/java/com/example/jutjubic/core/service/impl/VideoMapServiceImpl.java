package com.example.jutjubic.core.service.impl;

import com.example.jutjubic.api.dto.map.VideoMarkerDTO;
import com.example.jutjubic.core.service.VideoMapService;
import com.example.jutjubic.infrastructure.entity.VideoPostEntity;
import com.example.jutjubic.infrastructure.repository.JpaVideoPostRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    @Cacheable(value = "mapTiles", key = "#zoom + '_' + #minTileX + '_' + #maxTileX + '_' + #minTileY + '_' + #maxTileY")
    public List<VideoMarkerDTO> getVideosForTiles(int zoom, int minTileX, int maxTileX, int minTileY, int maxTileY) {
        // Konvertuj tile koordinate u geografske koordinate (lat/lng)
        double minLat = tile2lat(maxTileY + 1, zoom); // Y ide obrnuto
        double maxLat = tile2lat(minTileY, zoom);
        double minLng = tile2lon(minTileX, zoom);
        double maxLng = tile2lon(maxTileX + 1, zoom);

        return getVideosInBounds(minLat, maxLat, minLng, maxLng);
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