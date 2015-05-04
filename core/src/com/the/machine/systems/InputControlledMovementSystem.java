package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.the.machine.components.ControlComponent;
import com.the.machine.components.DirectionalVelocityComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.input.KeyDownEvent;
import com.the.machine.framework.events.input.KeyUpEvent;
import lombok.EqualsAndHashCode;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/03/15
 */
@EqualsAndHashCode
public class InputControlledMovementSystem extends IteratingSystem implements EventListener {

	transient private ComponentMapper<DirectionalVelocityComponent> velocities = ComponentMapper.getFor(DirectionalVelocityComponent.class);
	transient private ComponentMapper<ControlComponent> controls = ComponentMapper.getFor(ControlComponent.class);

	public InputControlledMovementSystem() {
		super(Family.all(ControlComponent.class, DirectionalVelocityComponent.class)
					.get());
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof KeyDownEvent || event instanceof KeyUpEvent) {
			int keycode = (event instanceof  KeyDownEvent) ? ((KeyDownEvent) event).getKeycode() : ((KeyUpEvent) event).getKeycode();
			int dir = (event instanceof KeyDownEvent) ? 100: -100;
			ImmutableArray<Entity> entities = getEntities();
			for (Entity entity : entities) {
				ControlComponent controlComponent = controls.get(entity);
				Control control = controlComponent.getControlMap()
												  .get(keycode);
				if (control != null) {
					DirectionalVelocityComponent velocityComponent = velocities.get(entity);
					switch (control) {
						case UP:
							velocityComponent.getVelocity().x += dir;
							break;
						case RIGHT:
							velocityComponent.getVelocity().y += dir;
							break;
						case DOWN:
							velocityComponent.getVelocity().x -= dir;
							break;
						case LEFT:
							velocityComponent.getVelocity().y -= dir;
							break;
					}
				}
			}
		}
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
	}

	public static enum Control {
		UP,
		RIGHT,
		DOWN,
		LEFT
	}

}
