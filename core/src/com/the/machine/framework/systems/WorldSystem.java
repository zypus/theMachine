package com.the.machine.framework.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
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

	public WorldSystem() {
		super(Family.all()
					.get());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(this);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}

	@Override
	public void entityAdded(Entity entity) {
		Entity worldEntity = world.getWorldEntityRef().get();
		if (worldEntity != null && !worldEntity.equals(entity) && !parents.has(entity)) {
			EntityUtilities.relate(worldEntity, entity);
		}
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
//		Entity worldEntity = world.getWorldEntityRef()
//								  .get();
//		if (worldEntity != null && !worldEntity.equals(entity) && !parents.has(entity) && !parents.get(entity).getParent().get().equals(worldEntity)) {
//			EntityUtilities.relate(worldEntity, entity);
//		}
	}
}
