package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.the.machine.components.DragComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.components.AngularVelocityComponent;
import com.the.machine.framework.components.TransformComponent;

/**
 * Created by Frans on 12-3-2015.
 */
public class RotationSystem extends IteratingSystem {
    private transient ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
    private transient ComponentMapper<AngularVelocityComponent> angularvelocities = ComponentMapper.getFor(AngularVelocityComponent.class);
    private transient ComponentMapper<DragComponent> draggedEntities = ComponentMapper.getFor(DragComponent.class);

    public RotationSystem() {
        super(Family.all(TransformComponent.class, AngularVelocityComponent.class)
                .get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Don't rotate the entity if it is currently being dragged
        if (!draggedEntities.has(entity)) {
            TransformComponent transformComponent = transforms.get(entity);
            AngularVelocityComponent angularVelocityComponent = angularvelocities.get(entity);

            float oldRotation = transformComponent.getZRotation(); // Rotation in degrees
            float newRotation = oldRotation + (deltaTime * angularVelocityComponent.getAngularVelocity());

            transformComponent.setZRotation(((newRotation % 360) + 360) % 360);
        }
    }
}
