package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.AreaComponent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.components.physics.Light2dComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.physics.ContactBeginEvent;
import com.the.machine.framework.events.physics.ContactEndEvent;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/03/15
 */
public class EnvironmentSystem extends AbstractSystem implements EventListener {

	transient private ComponentMapper<Light2dComponent> lights = ComponentMapper.getFor(Light2dComponent.class);
	transient private ComponentMapper<AgentComponent> agents = ComponentMapper.getFor(AgentComponent.class);
	transient private ComponentMapper<AreaComponent>  areas  = ComponentMapper.getFor(AreaComponent.class);

	@Override
	public void handleEvent(Event event) {
		if (event instanceof ContactBeginEvent) {
			Entity entity1 = ((ContactBeginEvent) event).getFirst()
														.get();
			Entity entity2 = ((ContactBeginEvent) event).getSecond()
														.get();
			if (entity1 != null && entity2 != null && !(lights.has(entity1) && lights.has(entity2) ) && !(areas.has(entity1) && areas.has(entity2))) {
				if (!lights.has(entity1)) {
					Entity temp = entity1;
					entity1 = entity2;
					entity2 = temp;
				}
				AreaComponent areaComponent = areas.get(entity2);
				AreaComponent.AreaType type = areaComponent.getType();
				AgentComponent agentComponent = agents.get(entity1);
				if (type == AreaComponent.AreaType.COVER) {
					agentComponent.setVisionModifier(0.5f);
					agentComponent.notifyObservers();
				}
				agentComponent.setEnvironmentType(type);
			}
		} else if (event instanceof ContactEndEvent) {
			Entity entity1 = ((ContactEndEvent) event).getFirst()
														.get();
			Entity entity2 = ((ContactEndEvent) event).getSecond()
														.get();
			if (entity1 != null && entity2 != null && !(lights.has(entity1) && lights.has(entity2)) && !(areas.has(entity1) && areas.has(entity2))) {
				if (!lights.has(entity1)) {
					Entity temp = entity1;
					entity1 = entity2;
					entity2 = temp;
				}
				AreaComponent areaComponent = areas.get(entity2);
				AreaComponent.AreaType type = areaComponent.getType();
				AgentComponent agentComponent = agents.get(entity1);
				if (type == AreaComponent.AreaType.COVER) {
					agentComponent.setVisionModifier(1);
					agentComponent.notifyObservers();
				}
				agentComponent.setEnvironmentType(AreaComponent.AreaType.GROUND);
			}
		}
	}
}
