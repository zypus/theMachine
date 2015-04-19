package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.DiscreteMapComponent;
import com.the.machine.framework.IntervalIteratingSystem;
import com.the.machine.framework.components.DelayedRemovalComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.ShapeRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.utility.BitBuilder;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/03/15
 */
@EqualsAndHashCode
public class DiscretizedMapDebugSystem
		extends IntervalIteratingSystem {

	transient private ComponentMapper<DiscreteMapComponent> discreteMaps = ComponentMapper.getFor(DiscreteMapComponent.class);
	transient private ComponentMapper<DimensionComponent>   dimensions   = ComponentMapper.getFor(DimensionComponent.class);

	public DiscretizedMapDebugSystem() {
		super(Family.all(DiscreteMapComponent.class)
					.get(), 1);
	}

	@Override
	protected void processEntity(Entity entity) {
		DiscreteMapComponent mapComponent = discreteMaps.get(entity);
		List<DiscreteMapComponent.MapCell> map = mapComponent.getSparseMap();
		DimensionComponent dm = dimensions.get(entity);
		for (DiscreteMapComponent.MapCell cell : map) {
			if (cell.getType() != AreaComponent.AreaType.GROUND && cell.getType() != AreaComponent.AreaType.OUTER_WALL) {
				Entity point = new Entity();
				point.add(new TransformComponent().setPosition(new Vector3(cell.getPosition().x, cell.getPosition().y, 0)));
				point.add(new ShapeRenderComponent().add((r) -> r.circle(0, 0, 1))
													.setSortingLayer("Physics 2d Debug"));
				point.add(new LayerComponent(BitBuilder.none(32)
													   .s(1)
													   .get()));
				point.add(new DelayedRemovalComponent().setDelay(1f));
				world.addEntity(point);
			}
		}
	}
}
