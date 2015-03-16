package com.the.machine.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frans on 15-3-2015.
 */
public class WorldMapComponent {
    public static final int cols = 5;   // Horizontal resolution
    public static final int rows = 5;   // Vertical resolution

    public static BiMap<Vector2, Entity> worldMap = HashBiMap.create();

    // TODO still needs a discrete map

    WorldMapComponent() {
        // Constructor should never be called
    }
}
