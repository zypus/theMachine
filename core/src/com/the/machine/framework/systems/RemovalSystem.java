package com.the.machine.framework.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.RemovalComponent;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/03/15
 */
public class RemovalSystem
		extends IteratingSystem {

	transient private ComponentMapper<RemovalComponent> removals = ComponentMapper.getFor(RemovalComponent.class);

	public RemovalSystem() {
		super(Family.one(RemovalComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		world.removeEntity(entity);
	}
}
