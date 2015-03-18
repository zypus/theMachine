package com.the.machine.framework.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.DelayedRemovalComponent;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/03/15
 */
public class DelayedRemovalSystem extends IteratingSystem {

	transient private ComponentMapper<DelayedRemovalComponent> delayedRemovals = ComponentMapper.getFor(DelayedRemovalComponent.class);

	public DelayedRemovalSystem() {
		super(Family.all(DelayedRemovalComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		DelayedRemovalComponent delayedRemovalComponent = delayedRemovals.get(entity);
		float delay = delayedRemovalComponent.getDelay();
		delay -= deltaTime;
		if (delay <= 0) {
			world.removeEntity(entity);
		} else {
			delayedRemovalComponent.setDelay(delay);
		}
	}
}
