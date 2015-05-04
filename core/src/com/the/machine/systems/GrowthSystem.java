package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.GrowthComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.TransformComponent;
import lombok.EqualsAndHashCode;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/03/15
 */
@EqualsAndHashCode
public class GrowthSystem extends IteratingSystem{

	transient private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<GrowthComponent> growth = ComponentMapper.getFor(GrowthComponent.class);

	public GrowthSystem() {
		super(Family.all(TransformComponent.class, GrowthComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		TransformComponent transformComponent = transforms.get(entity);
		GrowthComponent growthComponent = growth.get(entity);
		Vector3 growth = growthComponent.getGrowthRate().cpy().scl(deltaTime);
		transformComponent.setScale(transformComponent.getScale()
													  .cpy()
													  .add(growth));
	}
}
