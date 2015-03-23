package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.ListenerComponent;
import com.the.machine.components.MapGroundComponent;
import com.the.machine.components.VisionComponent;
import com.the.machine.events.AudioEvent;
import com.the.machine.events.DoorCancelEvent;
import com.the.machine.events.DoorOpenEvent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.utility.EntityUtilities;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/03/15
 */
public class DoorSystem extends AbstractSystem implements EventListener {

	private static final float FORCED_OPENING_TIME = 5;
	private static final float SILENT_OPENING_TIME = 12;
	private static final float SILENT_OPENING_TIME_STANDARD_DEVIATION = 2;

	transient private ComponentMapper<AreaComponent>      areas      = ComponentMapper.getFor(AreaComponent.class);
	transient private ComponentMapper<DimensionComponent> dimensions = ComponentMapper.getFor(DimensionComponent.class);
	transient private ComponentMapper<VisionComponent>    visions    = ComponentMapper.getFor(VisionComponent.class);
	transient private ComponentMapper<AgentComponent>     agents     = ComponentMapper.getFor(AgentComponent.class);
	transient private ComponentMapper<ListenerComponent>  listeners  = ComponentMapper.getFor(ListenerComponent.class);
	transient private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);

	transient private ImmutableArray<Entity> mapElements;
	transient private List<DoorCommand> commands = new ArrayList<>();

	transient private Random random = new Random();

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		mapElements = world.getEntitiesFor(Family.all(AreaComponent.class)
												 .exclude(MapGroundComponent.class)
												 .get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		mapElements = null;
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof DoorOpenEvent) {
			DoorOpenEvent openEvent = (DoorOpenEvent) event;
			Entity entity = openEvent.getOpener()
									 .get();
			boolean silent = openEvent.isSilent();
			AgentComponent agentComponent = agents.get(entity);
			if (agentComponent
						.getEnvironmentType() == AreaComponent.AreaType.DOOR_CLOSED) {
				Entity door = null;
				float dst2 = 10000000f;
				TransformComponent tf = EntityUtilities.computeAbsoluteTransform(entity);
				for (Entity element : mapElements) {
					AreaComponent areaComponent = areas.get(element);
					if (areaComponent.getType() == AreaComponent.AreaType.DOOR_CLOSED) {
						TransformComponent areaTf = EntityUtilities.computeAbsoluteTransform(element);
						float dist = areaTf.getPosition()
										   .dst2(tf.getPosition());
						if (dist < dst2) {
							dst2 = dist;
							door = element;
						}
					}
				}
				if (door != null) {
					agentComponent.setActing(true);
					float delay = FORCED_OPENING_TIME;
					if (silent) {
						delay = SILENT_OPENING_TIME + (float) (SILENT_OPENING_TIME_STANDARD_DEVIATION * random.nextGaussian());
					}
					commands.add(new DoorCommand(true, silent, delay, openEvent.getOpener(), new WeakReference<>(door)));
				}
			}
		} else if (event instanceof DoorCancelEvent) {
			Entity entity = ((DoorCancelEvent) event).getCanceler()
													 .get();
			DoorCommand toBeRemoved = null;
			for (DoorCommand command : commands) {
				if (command.actor.get() == entity) {
					toBeRemoved = command;
					break;
				}
			}
			if (toBeRemoved != null) {
				commands.remove(toBeRemoved);
			}
		}
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		List<DoorCommand> removeList = new ArrayList<>();
		for (DoorCommand command : commands) {
			command.delay -= deltaTime;
			if (command.delay <= 0) {
				Entity entity = command.actor.get();
				Entity door = command.door.get();
				AgentComponent agentComponent = agents.get(entity);
				agentComponent
					  .setActing(false);
				agentComponent.setEnvironmentType(AreaComponent.AreaType.DOOR_OPEN);
				areas.get(door)
					 .setType(AreaComponent.AreaType.DOOR_OPEN);
				TransformComponent tf = EntityUtilities.computeAbsoluteTransform(entity);
				if (!command.silent) {
					world.dispatchEvent(new AudioEvent(tf.getPosition(), 5, command.actor));
				}
				removeList.add(command);
			}
		}
		for (DoorCommand command : removeList) {
			commands.remove(command);
		}

	}

	@AllArgsConstructor
	@Data
	private static class DoorCommand {
		boolean               opening;
		boolean               silent;
		float                 delay;
		WeakReference<Entity> actor;
		WeakReference<Entity> door;
	}
}
