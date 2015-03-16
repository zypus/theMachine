package com.the.machine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.BiMap;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.WorldMapComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.TransformComponent;

import java.util.Map;

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

        BiMap<Vector2, Entity> worldMap = WorldMapComponent.worldMap;

        // If the entity is already on the map
        if (worldMap.containsValue(entity)) {
            worldMap.inverse().remove(entity);
        }

        // Add the entity to the bimap (and to entitiesOnMap)
        worldMap.put(transformComponent.get2DPosition(), entity);
    }
}
