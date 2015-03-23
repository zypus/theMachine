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
import com.the.machine.events.WindowDestroyEvent;
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
public class WindowSystem extends AbstractSystem implements EventListener {

	private static final float FORCED_OPENING_TIME                    = 3;
	private static final float NOISE_RANGE                    = 10;

	transient private ComponentMapper<AreaComponent>      areas      = ComponentMapper.getFor(AreaComponent.class);
	transient private ComponentMapper<DimensionComponent> dimensions = ComponentMapper.getFor(DimensionComponent.class);
	transient private ComponentMapper<VisionComponent>    visions    = ComponentMapper.getFor(VisionComponent.class);
	transient private ComponentMapper<AgentComponent>     agents     = ComponentMapper.getFor(AgentComponent.class);
	transient private ComponentMapper<ListenerComponent>  listeners  = ComponentMapper.getFor(ListenerComponent.class);
	transient private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);

	transient private ImmutableArray<Entity> mapElements;
	transient private List<WindowCommand> commands = new ArrayList<>();

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
		if (event instanceof WindowDestroyEvent) {
			WindowDestroyEvent windowEvent = (WindowDestroyEvent) event;
			Entity entity = windowEvent.getDestroyer()
									 .get();
			AgentComponent agentComponent = agents.get(entity);
			if (agentComponent
						.getEnvironmentType() == AreaComponent.AreaType.WINDOW) {
				Entity window = null;
				float dst2 = 10000000f;
				TransformComponent tf = EntityUtilities.computeAbsoluteTransform(entity);
				for (Entity element : mapElements) {
					AreaComponent areaComponent = areas.get(element);
					if (areaComponent.getType() == AreaComponent.AreaType.WINDOW) {
						TransformComponent areaTf = EntityUtilities.computeAbsoluteTransform(element);
						float dist = areaTf.getPosition()
										   .dst2(tf.getPosition());
						if (dist < dst2) {
							dst2 = dist;
							window = element;
						}
					}
				}
				if (window != null) {
					agentComponent.setActing(true);
					commands.add(new WindowCommand(FORCED_OPENING_TIME, windowEvent.getDestroyer(), new WeakReference<>(window)));
				}
			}
		}
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		List<WindowCommand> removeList = new ArrayList<>();
		for (WindowCommand command : commands) {
			command.delay -= deltaTime;
			if (command.delay <= 0) {
				Entity entity = command.actor.get();
				Entity door = command.window.get();
				AgentComponent agentComponent = agents.get(entity);
				agentComponent
						.setActing(false);
				agentComponent.setEnvironmentType(AreaComponent.AreaType.WINDOW_BROKEN);
				areas.get(door)
					 .setType(AreaComponent.AreaType.WINDOW_BROKEN);
				TransformComponent tf = EntityUtilities.computeAbsoluteTransform(entity);
				world.dispatchEvent(new AudioEvent(tf.getPosition(), NOISE_RANGE, command.actor));
				removeList.add(command);
			}
		}
		for (WindowCommand command : removeList) {
			commands.remove(command);
		}

	}

	@AllArgsConstructor
	@Data
	private static class WindowCommand {
		float                 delay;
		WeakReference<Entity> actor;
		WeakReference<Entity> window;
	}

}
