package com.the.machine.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frans on 15-3-2015.
 */
public class WorldMapComponent {
    public static final int cols = 5;   // Horizontal resolution
    public static final int rows = 5;   // Vertical resolution

    public static Map<Vector2, AreaComponent.AreaType> worldMap = new HashMap<>();
    public static Map<Entity, Vector2> entitiesOnMap = new HashMap<>();

    // TODO still needs a discrete map

    WorldMapComponent() {
        // Constructor should never be called
    }
}
