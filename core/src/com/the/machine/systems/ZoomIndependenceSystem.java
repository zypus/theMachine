package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.the.machine.components.ZoomIndependentComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.DisabledComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.TransformComponent;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 15/03/15
 */
public class ZoomIndependenceSystem extends IteratingSystem {

	transient private ComponentMapper<TransformComponent> transforms       = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<DisabledComponent>  disabled         = ComponentMapper.getFor(DisabledComponent.class);
	transient private ComponentMapper<CameraComponent>    cameraComponents = ComponentMapper.getFor(CameraComponent.class);
	transient private ComponentMapper<LayerComponent>     layers           = ComponentMapper.getFor(LayerComponent.class);

	transient private ImmutableArray<Entity> cameras = null;

	public ZoomIndependenceSystem() {
		super(Family.all(ZoomIndependentComponent.class, TransformComponent.class)
					.get());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		cameras = world.getEntitiesFor(Family.all(CameraComponent.class)
													  .get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		cameras = null;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		for (Entity camera : cameras) {
			CameraComponent cameraComponent = cameraComponents.get(camera);
			LayerComponent layerComponent = layers.get(entity);
			if (layerComponent != null && cameraComponent.getCullingMask() != null && cameraComponent.getCullingMask()
																									 .containsAll(layerComponent.getLayer())) {
				Float zoom = cameraComponent.getZoom();
				transforms.get(entity)
						  .setScale(zoom);
			}
		}
	}
}
