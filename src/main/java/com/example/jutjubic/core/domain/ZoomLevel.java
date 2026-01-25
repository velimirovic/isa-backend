package com.example.jutjubic.core.domain;

public enum ZoomLevel {
    CONTINENT(0, 4, 1),      // zoom 0-4: prikaži 1 video po tile-u
    COUNTRY(5, 8, 5),        // zoom 5-8: prikaži 5 videa po tile-u
    REGION(9, 12, 20),       // zoom 9-12: prikaži 20 videa po tile-u
    CITY(13, 15, 50),        // zoom 13-15: prikaži 50 videa po tile-u
    STREET(16, 18, 200);     // zoom 16-18: prikaži sve (ili 200 max)

    private final int minZoom;
    private final int maxZoom;
    private final int maxVideosPerTile;

    ZoomLevel(int minZoom, int maxZoom, int maxVideosPerTile) {
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.maxVideosPerTile = maxVideosPerTile;
    }

    public static ZoomLevel fromZoom(int zoom) {
        for (ZoomLevel level : values()) {
            if (zoom >= level.minZoom && zoom <= level.maxZoom) {
                return level;
            }
        }
        return STREET; // default
    }

    public int getMaxVideosPerTile() {
        return maxVideosPerTile;
    }
}
