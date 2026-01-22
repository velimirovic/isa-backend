package com.example.jutjubic.api.controller;

import com.example.jutjubic.api.dto.map.VideoMarkerDTO;
import com.example.jutjubic.core.service.VideoMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/map")
public class VideoMapController {

    private final VideoMapService videoMapService;

    // Endpoint za dobijanje videa u bounding box-u
    // GET /api/map/videos?minLat=44.5&maxLat=45.5&minLng=20.0&maxLng=21.0
    @GetMapping("/videos")
    public ResponseEntity<List<VideoMarkerDTO>> getVideosInBounds(
            @RequestParam("minLat") double minLat,
            @RequestParam("maxLat") double maxLat,
            @RequestParam("minLng") double minLng,
            @RequestParam("maxLng") double maxLng) {
        try {
            List<VideoMarkerDTO> videos = videoMapService
                    .getVideosInBounds(minLat, maxLat, minLng, maxLng);
            return ResponseEntity.ok(videos);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Endpoint za dobijanje videa za određene tiles
    // GET /api/map/tiles?zoom=6&minTileX=32&maxTileX=35&minTileY=20&maxTileY=23

    @GetMapping("/tiles")
    public ResponseEntity<List<VideoMarkerDTO>> getVideosForTiles(
            @RequestParam("zoom") int zoom,
            @RequestParam("minTileX") int minTileX,
            @RequestParam("maxTileX") int maxTileX,
            @RequestParam("minTileY") int minTileY,
            @RequestParam("maxTileY") int maxTileY) {
        try {
            List<VideoMarkerDTO> videos = videoMapService
                    .getVideosForTiles(zoom, minTileX, maxTileX, minTileY, maxTileY);
            return ResponseEntity.ok(videos);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}