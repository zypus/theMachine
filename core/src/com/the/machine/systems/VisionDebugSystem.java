package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.DiscreteMapComponent;
import com.the.machine.components.VisionComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.DelayedRemovalComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.ShapeRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.utility.BitBuilder;

import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/03/15
 */
public class VisionDebugSystem
		extends IteratingSystem {

	transient private ComponentMapper<VisionComponent> visions = ComponentMapper.getFor(VisionComponent.class);
	transient private ComponentMapper<DimensionComponent>   dimensions   = ComponentMapper.getFor(DimensionComponent.class);

	public VisionDebugSystem() {
		super(Family.all(VisionComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		VisionComponent visionComponent = visions.get(entity);
		List<DiscreteMapComponent.MapCell> visibleCells = visionComponent.getVisibleCells();
		for (DiscreteMapComponent.MapCell cell : visibleCells) {
			if (cell.getType() != AreaComponent.AreaType.GROUND) {
				Entity point = new Entity();
				point.add(new TransformComponent().setPosition(new Vector3(cell.getPosition().x, cell.getPosition().y, 0)));
				point.add(new ShapeRenderComponent().add((r) -> r.rect(-0.5f, -0.5f, 1, 1))
													.setSortingLayer("Physics 2d Debug"));
				point.add(new LayerComponent(BitBuilder.none(32)
													   .s(1)
													   .get()));
				point.add(new DelayedRemovalComponent().setDelay(0.25f));
				world.addEntity(point);
			}
		}
	}
}
