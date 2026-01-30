package com.example.jutjubic.core.service;

import com.example.jutjubic.api.dto.map.VideoMarkerDTO;
import com.example.jutjubic.core.domain.FilterType;

import java.util.List;

public interface VideoMapService {
    List<VideoMarkerDTO> getVideosInBounds(double minLat, double maxLat, double minLng, double maxLng);
    List<VideoMarkerDTO> getVideosForTiles(int zoom, int minTileX, int maxTileX, int minTileY, int maxTileY, FilterType filter);
    List<VideoMarkerDTO> getVideosInBoundsWithLimit(double minLat, double maxLat, double minLng, double maxLng, int limit, FilterType filter);
    void refreshAllTileCache();
}