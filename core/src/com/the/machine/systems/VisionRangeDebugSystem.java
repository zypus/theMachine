package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.SprintComponent;
import com.the.machine.components.VisionComponent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.components.AbstractComponent;
import com.the.machine.framework.components.ShapeRenderComponent;
import com.the.machine.framework.interfaces.Observable;
import com.the.machine.framework.interfaces.Observer;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/03/15
 */
public class VisionRangeDebugSystem extends AbstractSystem implements Observer, EntityListener {

	transient private ComponentMapper<VisionComponent> visions = ComponentMapper.getFor(VisionComponent.class);
	transient private ComponentMapper<ShapeRenderComponent> shapes = ComponentMapper.getFor(ShapeRenderComponent.class);
	transient private ComponentMapper<AgentComponent> agents = ComponentMapper.getFor(AgentComponent.class);
	transient private ComponentMapper<SprintComponent> sprints = ComponentMapper.getFor(SprintComponent.class);

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(Family.all(VisionComponent.class, ShapeRenderComponent.class)
									   .get(), this);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}

	@Override
	public void entityAdded(Entity entity) {
		visions.get(entity)
			   .addObserver(this);
		agents.get(entity)
			  .addObserver(this);
	}

	@Override
	public void entityRemoved(Entity entity) {
		visions.get(entity)
			   .deleteObserver(this);
		agents.get(entity)
			  .deleteObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		Entity entity = ((AbstractComponent) o).getOwner()
											   .get();
		if (entity != null) {
			VisionComponent visionComponent = visions.get(entity);
			ShapeRenderComponent renderComponent = shapes.get(entity);
			AgentComponent agentComponent = agents.get(entity);
			VisionRangeDebug debug = ((VisionRangeDebug) renderComponent.getShapes()
																		.get(0));
			float blind = (visionComponent.isBlind())
						  ? 0
						  : 1;
			debug.setMin(visionComponent.getMinDistance() * agentComponent.getVisionModifier() * blind);
			debug.setMax(visionComponent.getMaxDistance() * agentComponent.getVisionModifier() * blind);
			debug.setStructures(10 * agentComponent.getVisionModifier() * blind);
			debug.setTowers(18 * agentComponent.getVisionModifier() * blind);
			debug.setAngle(visionComponent.getAngle());
		}
	}

	@AllArgsConstructor
	@Data
	public static class VisionRangeDebug
			implements ShapeRenderComponent.Shape {

		private Color color;
		private float min;
		private float max;
		private float angle;
		private float structures;
		private float towers;

		@Override
		public void render(ShapeRenderer r) {
			r.setColor(color);
			r.circle(0, 0, min);
			r.circle(0, 0, max);
			r.circle(0, 0, structures);
			r.circle(0, 0, towers);
			float a = MathUtils.degreesToRadians * angle / 2;
			r.line((float) (min * Math.cos(a)), (float) (min * Math.sin(a)), (float) (towers * Math.cos(a)), (float) (towers * Math.sin(a)));
			r.line((float) (min * Math.cos(a)), (float) -(min * Math.sin(a)), (float) (towers * Math.cos(a)), (float) -(towers * Math.sin(a)));
		}
	}
}
