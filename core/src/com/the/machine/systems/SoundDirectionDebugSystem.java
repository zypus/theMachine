package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.ListenerComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.DelayedRemovalComponent;
import com.the.machine.framework.components.ShapeRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.utility.EntityUtilities;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/03/15
 */
@EqualsAndHashCode
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
		for (Vector2 vector2 : listenerComponent.getSoundDirections()) {
			Entity indicator = new Entity();
			indicator.add(new ShapeRenderComponent().setSortingLayer("Physics 2d Debug")
													.add(new LineShape(vector2.cpy().scl(10))));
			indicator.add(new TransformComponent());
			indicator.add(new DelayedRemovalComponent().setDelay(0.1f));
			EntityUtilities.relate(entity, indicator);
			world.addEntity(indicator);
		}
	}

	@AllArgsConstructor
	public static class LineShape implements ShapeRenderComponent.Shape {

		private Vector2 dir;

		@Override
		public void render(ShapeRenderer r) {
			r.line(0, 0, dir.x, dir.y);
		}
	}
}
