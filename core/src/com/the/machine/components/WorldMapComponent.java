package com.the.machine.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.the.machine.framework.components.AbstractComponent;
import com.the.machine.framework.components.DimensionComponent;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Frans on 15-3-2015.
 */
public class WorldMapComponent extends AbstractComponent{
    private int cols;   // Horizontal resolution
    private int rows;   // Vertical resolution

    // The area that will be mapped is determined in the DimensionComponent of the entity that has the MapGroundComponent
    public float x_start;
    public float x_end;
    public float y_start;
    public float y_end;

    public float col_width(){ return Math.abs(x_end - x_start) / (cols - 1); }
    public float row_height(){ return Math.abs(y_end - y_start) / (rows - 1); }

    public static Map<Vector2, Entity> worldMap = new HashMap<>(); // Maps coordinates to entities

    public WorldMapComponent() {
        x_start = -1;
        x_end = 1;
        y_start = -1;
        y_end = 1;

        cols = 10;
        rows = 10;
    }

    public WorldMapComponent setDimension(DimensionComponent dc) {
        worldMap = new HashMap<>();

        float width = dc.getWidth();
        float height = dc.getHeight();

        float x_start = -width/2;
        float y_start = -height/2;
        float x_end = width/2;
        float y_end = height/2;

        return this;
    }

    public WorldMapComponent setResolution(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        return this;
    }
}
