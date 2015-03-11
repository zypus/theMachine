package com.the.machine.framework.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.DirectionComponent;
import com.the.machine.framework.components.TransformComponent;

/**
 * Created by Frans on 25-2-2015.
 */
public class MovementSystem extends IteratingSystem {

    public MovementSystem() {
        // Act on Entities with a Direction Component
        super(Family.one(DirectionComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transformComponent = entity.getComponent(TransformComponent.class);
        DirectionComponent directionComponent = entity.getComponent(DirectionComponent.class);

        // Multiply the direction vector with the time scalar
        Vector3 travelled = directionComponent.getDirection().scl(deltaTime);
        transformComponent.getPosition().add(travelled);
    }
}
