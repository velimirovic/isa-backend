package com.example.jutjubic.core.service;

import com.example.jutjubic.api.dto.map.VideoMarkerDTO;
import java.util.List;

public interface VideoMapService {
    List<VideoMarkerDTO> getVideosInBounds(double minLat, double maxLat, double minLng, double maxLng);
    List<VideoMarkerDTO> getVideosForTiles(int zoom, int minTileX, int maxTileX, int minTileY, int maxTileY);
}