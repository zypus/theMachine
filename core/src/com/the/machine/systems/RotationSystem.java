package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.VelocityComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.AngularVelocityComponent;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.TransformComponent;

/**
 * Created by Frans on 12-3-2015.
 */
public class RotationSystem extends IteratingSystem {
    private transient ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
    private transient ComponentMapper<AngularVelocityComponent> angularvelocities = ComponentMapper.getFor(AngularVelocityComponent.class);

    public RotationSystem() {
        super(Family.all(TransformComponent.class, AngularVelocityComponent.class)
                .get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transformComponent = transforms.get(entity);
        AngularVelocityComponent angularVelocityComponent = angularvelocities.get(entity);

        float oldRotation = transformComponent.getZRotation(); // Rotation in degrees
        float newRotation = oldRotation + (deltaTime * angularVelocityComponent.getAngularVelocity());

        if (newRotation >= 0)
            newRotation = newRotation % 360;
        else
            newRotation += 360;

        transformComponent.setZRotation(newRotation);
    }
}
