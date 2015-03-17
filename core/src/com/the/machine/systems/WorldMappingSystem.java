package com.the.machine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.WorldMapComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.TransformComponent;

import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Frans on 15-3-2015.
 */
public class WorldMappingSystem extends IteratingSystem {
    public WorldMappingSystem(int priority) {
        super(Family.all(
                AreaComponent.class,
                TransformComponent.class
        ).get(), priority);
    }
    
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        /*
         * The map only contains elements that contain an AreaComponent and a TransformComponent. The AreaComponent is
         * not directly stored, but is required to distinguish between different entity types
         */
        TransformComponent transformComponent = entity.getComponent(TransformComponent.class);
        Map<Vector2, Entity> worldMap = WorldMapComponent.worldMap;

        /*
         * If the entity is not yet on the map, add it.
         *
         * This means that the WorldMappingSystem only adds new Entities (if they have an AreaComponent and
         * TransformComponent). The system won't delete entities that are removed, and won't update them if they are
         * resized or moved.
         * If the AreaType of an entity is changed, the map doesn't need to change, because the coordinates are mapped to
         * entities, not AreaComponents.
         */
        if (!worldMap.containsValue(entity)) {
            Vector2 bottomLeft = new Vector2(
                    (float) (transformComponent.getX() - 0.5 * transformComponent.getXScale()),
                    (float) (transformComponent.getY() - 0.5 * transformComponent.getYScale()));

            Rectangle.Float entitySurface = new Rectangle.Float(bottomLeft.x, bottomLeft.y, transformComponent.getXScale(), transformComponent.getYScale());

            // Areas are non-overlapping, so we can put the coordinates within the rectangle on the WorldMap
            Set<Vector2> coordinatesOfEntity = new HashSet<>();
            int startRow = (int) (Math.round(entitySurface.y / WorldMapComponent.row_height));
            int endRow   = (int) (Math.round((entitySurface.y + entitySurface.height) / WorldMapComponent.row_height));
            int startCol = (int) (Math.round(entitySurface.x / WorldMapComponent.col_width));
            int endCol   = (int) (Math.round((entitySurface.x + entitySurface.width) / WorldMapComponent.col_width));

            for (int row = startRow; row <= endRow; row++) {
                for (int col = startCol; col <= endCol; col++) {
                    coordinatesOfEntity.add(new Vector2(
                            col * WorldMapComponent.col_width,
                            row * WorldMapComponent.row_height));
                }
            }

            // Also put the coordinates on the worldmap (so we can look up entities based on their coordinate)
            for (Vector2 coordinate : coordinatesOfEntity) {
                /*
                 * Due to rounding, one coordinate can be close to multiple entities. If so, the resolution should be
                 * increased in WorldMapComponent.
                 * To avoid this, we check if the WorldMap already contains the coordinate.
                 */
                if (!worldMap.containsKey(coordinate)) {
                    worldMap.put(coordinate, entity);

//                    // For debugging. Add a white pixel to areas that are on the map
//                    Entity white = new Entity();
//                    white.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch("white", TextureRegion.class)).setSortingOrder(-1));
//                    white.add(new TransformComponent().set2DPosition(coordinate).setScale(0.01f));
//                    getWorld().addEntity(white);
                }
            }
        }
    }
}
