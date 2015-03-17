package com.the.machine.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Frans on 15-3-2015.
 */
public class WorldMapComponent {
    private static final int cols = 100;   // Horizontal resolution
    private static final int rows = 100;   // Vertical resolution

    // The area that will be mapped
    public static final float x_start = -2;
    public static final float x_end = 2;
    public static final float y_start = -2;
    public static final float y_end = 2;

    public static final float col_width = Math.abs(x_end - x_start) / (cols - 1);
    public static final float row_height = Math.abs(y_end - y_start) / (rows - 1);

    public static BiMap<Set<Vector2>, Entity> worldBiMap = HashBiMap.create();  // Maps a set of coordinates to an entity and vice versa
    public static Map<Vector2, Entity> worldMap = new HashMap<>(); // Maps coordinates to entities

    WorldMapComponent() {
        // Constructor should never be called
    }
}
