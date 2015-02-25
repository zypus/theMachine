package com.the.machine.framework.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.utility.EntityUtilities;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/02/15
 */
public class WorldSystem extends IteratingSystem implements EntityListener {

	transient private ComponentMapper<ParentComponent> parents = ComponentMapper.getFor(ParentComponent.class);

	private WorldSystem() {
		super(Family.all()
					.get());
	}

	@Override
	public void entityAdded(Entity entity) {

	}

	@Override
	public void entityRemoved(Entity entity) {
		if (parents.has(entity)) {
			Entity parent = parents.get(entity)
									.getParent()
									.get();
			EntityUtilities.derelate(parent, entity);
		}
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {

	}
}
