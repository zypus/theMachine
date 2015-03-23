package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.DirectionalVelocityComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.TransformComponent;
import lombok.EqualsAndHashCode;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/03/15
 */
@EqualsAndHashCode
public class DirectionalMovementSystem extends IteratingSystem {

	private transient ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	private transient ComponentMapper<DirectionalVelocityComponent>  velocities = ComponentMapper.getFor(DirectionalVelocityComponent.class);

	public DirectionalMovementSystem() {
		super(Family.all(TransformComponent.class, DirectionalVelocityComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		TransformComponent transformComponent = transforms.get(entity);
		DirectionalVelocityComponent velocityComponent = velocities.get(entity);
		Vector3 velocity = velocityComponent.getVelocity().cpy();
		Quaternion rotation = transformComponent.getRotation();
		Vector3 deltaPosition = velocity.scl(deltaTime);
		rotation.transform(deltaPosition);
		transformComponent.setPosition(transformComponent.getPosition().cpy().add(deltaPosition));
		transformComponent.notifyObservers();
	}
}
