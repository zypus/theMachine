package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.ListenerComponent;
import com.the.machine.events.AudioEvent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.utility.EntityUtilities;
import lombok.EqualsAndHashCode;

import java.util.Random;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/03/15
 */
@EqualsAndHashCode
public class AudioListeningSystem extends AbstractSystem implements EventListener {

	transient private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<ListenerComponent> listenerComponents = ComponentMapper.getFor(ListenerComponent.class);

	transient private ImmutableArray<Entity> listeners;

	transient private Random random = new Random();

	private static final float STANDART_DEVIATION = 10;

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		listeners = engine.getEntitiesFor(Family.all(TransformComponent.class, ListenerComponent.class)
												.get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		listeners = null;
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof AudioEvent) {
			AudioEvent audioEvent = (AudioEvent) event;
			Vector3 location = audioEvent.getLocation().cpy();
			location.z = 0;
			Entity source = audioEvent.getSource()
									  .get();

			float dist2 = audioEvent.getHearableDistance() * audioEvent.getHearableDistance();
			for (Entity listener : listeners) {
				if (source != listener) {
					ListenerComponent listenerComponent = listenerComponents.get(listener);
					if (!listenerComponent.isDeaf()) {
						TransformComponent transform = transforms.get(listener);
						Vector3 listenerPos = transform.getPosition().cpy();
						listenerPos.z = 0;
						if (listenerPos.dst2(location) < dist2) {
							Vector3 dir = EntityUtilities.inLocalCoordinates(listener, location);
							//							Vector3 dir = listenerPos.cpy()
							//												  .sub(location);
							// normal distribution with standard deviation of 10 degrees
							dir.rotate((float) (STANDART_DEVIATION*random.nextGaussian()), 0, 0, 1);
							dir.nor();
							//							transform.getRotation()
							//									 .transform(dir);
							listenerComponent
									.getSoundDirections()
									.add(new Vector2(dir.x, dir.y));
						}
					}
				}
			}
		}
	}
}
