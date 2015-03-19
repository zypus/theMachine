package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.DragComponent;
import com.the.machine.components.VelocityComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.TransformComponent;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 25/02/15
 */
public class MovementSystem extends IteratingSystem {

	private transient ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	private transient ComponentMapper<VelocityComponent> velocities = ComponentMapper.getFor(VelocityComponent.class);
    private transient ComponentMapper<DragComponent>     draggedEntities = ComponentMapper.getFor(DragComponent.class);

	public MovementSystem() {
		super(Family.all(TransformComponent.class, VelocityComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
        // Don't move the entity if it is currently being dragged by the mouse
        if (!draggedEntities.has(entity)) {
            TransformComponent transformComponent = transforms.get(entity);
            VelocityComponent velocityComponent = velocities.get(entity);
            float velocity = velocityComponent.getVelocity();
            Quaternion rotation = transformComponent.getRotation();
            Vector3 deltaPosition = new Vector3(1, 0, 0).scl(velocity * deltaTime);
            rotation.transform(deltaPosition);
            transformComponent.setPosition(transformComponent.getPosition().cpy().add(deltaPosition));
            transformComponent.notifyObservers();
        }
	}
}
