package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.the.machine.components.ZoomableComponent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.input.ScrolledEvent;
import lombok.EqualsAndHashCode;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 14/03/15
 */
@EqualsAndHashCode
public class CameraZoomSystem extends AbstractSystem implements EventListener {

	transient private ComponentMapper<CameraComponent> cameraComponents = ComponentMapper.getFor(CameraComponent.class);

	transient private ImmutableArray<Entity> cameras = null;

	@Override
	public void handleEvent(Event event) {
		if (event instanceof ScrolledEvent) {
			if (cameras != null) {
				for (Entity camera : cameras) {
					CameraComponent cameraComponent = cameraComponents.get(camera);
					float zoom = cameraComponent.getZoom() + 0.05f * ((ScrolledEvent) event).getAmount();
					if (zoom <= 0) {
						zoom = 0.01f;
					}
					cameraComponent.setZoom(zoom);
				}
			}
		}
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		cameras = world.getEntitiesFor(Family.all(CameraComponent.class, ZoomableComponent.class)
											 .get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		cameras = null;
	}
}
