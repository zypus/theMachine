package com.the.machine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
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
        AreaComponent areaComponent = entity.getComponent(AreaComponent.class);
        TransformComponent transformComponent = entity.getComponent(TransformComponent.class);

        Map<Vector2, AreaComponent.AreaType> worldMap = WorldMapComponent.worldMap;
        Map<Entity, Vector2> entitiesOnMap = WorldMapComponent.entitiesOnMap;

        // If the entity is already on the map
        if (entitiesOnMap.containsKey(entity)) {
            worldMap.remove(entitiesOnMap.get(entity));
            entitiesOnMap.remove(entity);
        }

        // Add the entity to the map (and to entitiesOnMap)
        worldMap.put(transformComponent.get2DPosition(), areaComponent.getType());
        entitiesOnMap.put(entity, transformComponent.get2DPosition());
    }
}
