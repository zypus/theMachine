package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.DragComponent;
import com.the.machine.components.VelocityComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.physics.Physics2dComponent;
import lombok.EqualsAndHashCode;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 25/02/15
 */

@EqualsAndHashCode
public class MovementSystem
        extends IteratingSystem {

    private transient ComponentMapper<TransformComponent> transforms      = ComponentMapper.getFor(TransformComponent.class);
    private transient ComponentMapper<VelocityComponent>  velocities      = ComponentMapper.getFor(VelocityComponent.class);
    private transient ComponentMapper<Physics2dComponent> physics         = ComponentMapper.getFor(Physics2dComponent.class);
    private transient ComponentMapper<DragComponent>      draggedEntities = ComponentMapper.getFor(DragComponent.class);

    public MovementSystem() {
        super(Family.all(TransformComponent.class, VelocityComponent.class, Physics2dComponent.class)
                    .get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Don't move the entity if it is currently being dragged by the mouse
        if (!draggedEntities.has(entity)) {
            TransformComponent transformComponent = transforms.get(entity);
            VelocityComponent velocityComponent = velocities.get(entity);
            Physics2dComponent physics2dComponent = physics.get(entity);
            if (velocityComponent.isDirty()) {
                float velocity = velocityComponent.getVelocity();
                Quaternion rotation = transformComponent.getRotation();
                Vector3 deltaPosition = new Vector3(1, 0, 0).scl(velocity);
                rotation.transform(deltaPosition);
                physics2dComponent.getBody()
                                  .setLinearVelocity(new Vector2(deltaPosition.x / 10, deltaPosition.y / 10));
                velocityComponent.setDirty(false);
            } else {
                velocityComponent.setVelocity(physics2dComponent.getBody()
                                                                .getLinearVelocity()
                                                                .len() * 10);
            }
            if (!world.getSystem(BehaviourSystem.class).checkProcessing()) {
                physics2dComponent.getBody()
                                  .setLinearVelocity(new Vector2(0,0));
            }

        }
    }
}
