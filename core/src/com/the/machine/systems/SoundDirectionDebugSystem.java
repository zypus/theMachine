package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.ListenerComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.DelayedRemovalComponent;
import com.the.machine.framework.components.ShapeRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.utility.EntityUtilities;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/03/15
 */
public class SoundDirectionDebugSystem extends IteratingSystem {

	transient private ComponentMapper<TransformComponent> transforms         = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<ListenerComponent>  listenerComponents = ComponentMapper.getFor(ListenerComponent.class);

	public SoundDirectionDebugSystem() {
		super(Family.all(ListenerComponent.class, TransformComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		TransformComponent transformComponent = transforms.get(entity);
		ListenerComponent listenerComponent = listenerComponents.get(entity);
		for (Vector3 vector3 : listenerComponent.getSoundDirections()) {
			Entity indicator = new Entity();
			indicator.add(new ShapeRenderComponent().setSortingLayer("Physics 2d Debug").add((r) -> r.line(0,0, vector3.x*10, vector3.y*10)));
			indicator.add(new TransformComponent());
			indicator.add(new DelayedRemovalComponent().setDelay(1));
			EntityUtilities.relate(entity, indicator);
			world.addEntity(indicator);
		}
		listenerComponent.getSoundDirections().clear();
	}
}
