package com.the.machine.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.MapGroundComponent;
import com.the.machine.components.WorldMapComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.TransformComponent;

import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Frans on 15-3-2015.
 */
public class WorldMappingSystem extends IteratingSystem {
    private ImmutableArray<Entity> groundComponentEntities;

    public WorldMappingSystem(int priority) {
        // Entities that show on the map
        super(Family.all(
                AreaComponent.class,
                TransformComponent.class).get(), priority);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        groundComponentEntities = engine.getEntitiesFor(Family.all(MapGroundComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        groundComponentEntities = null;
    }

    @Override
    protected void processEntity(Entity areaEntity, float deltaTime) {
        /*
         * Entities are only processed if there is at least one GroundComponent in the world
         */
        if (groundComponentEntities.size() > 0) {
            TransformComponent areaTransformComponent = areaEntity.getComponent(TransformComponent.class);
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

            WorldMapComponent wmc = groundComponentEntities.get(0).getComponent(WorldMapComponent.class);
            if (!worldMap.containsValue(areaTransformComponent)) {
                Vector2 bottomLeft = new Vector2(
                        (float) (areaTransformComponent.getX() - 0.5 * areaTransformComponent.getXScale()),
                        (float) (areaTransformComponent.getY() - 0.5 * areaTransformComponent.getYScale()));

                Rectangle.Float entitySurface = new Rectangle.Float(bottomLeft.x, bottomLeft.y, areaTransformComponent.getXScale(), areaTransformComponent.getYScale());

                // Areas are non-overlapping, so we can put the coordinates within the rectangle on the WorldMap
                Set<Vector2> coordinatesOfEntity = new HashSet<>();
                int startRow = (int) (Math.round(entitySurface.y / wmc.row_height()));
                int endRow = (int) (Math.round((entitySurface.y + entitySurface.height) / wmc.row_height()));
                int startCol = (int) (Math.round(entitySurface.x / wmc.col_width()));
                int endCol = (int) (Math.round((entitySurface.x + entitySurface.width) / wmc.col_width()));

                for (int row = startRow; row <= endRow; row++) {
                    for (int col = startCol; col <= endCol; col++) {
                        coordinatesOfEntity.add(new Vector2(
                                col * wmc.col_width(),
                                row * wmc.row_height()));
                    }
                }

                // Also put the coordinates on the worldmap (so we can look up entities based on their coordinate)
                for (Vector2 coordinate : coordinatesOfEntity) {
                    /*
                     * Due to rounding, one coordinate can be closer to other entities. If this causes problems, the
                     * resolution should be increased in WorldMapComponent.
                     * To avoid this, we check if the WorldMap already contains the coordinate.
                     */
                    if (!worldMap.containsKey(coordinate)) {
                        worldMap.put(coordinate, areaEntity);
                    }
                }
            }
        }
        else {
            // If there is no MapGroundComponent found
            System.err.println("WorldMappingSystem: No entity with the MapGroundComponent found.");
        }
    }
}
