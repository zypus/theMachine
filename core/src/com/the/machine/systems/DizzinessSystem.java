package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.AngularVelocityComponent;
import com.the.machine.components.DizzinessComponent;
import com.the.machine.components.VisionComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.physics.Light2dComponent;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/03/15
 */
public class DizzinessSystem extends IteratingSystem {

	transient private ComponentMapper<DizzinessComponent> dizziness = ComponentMapper.getFor(DizzinessComponent.class);
	transient private ComponentMapper<AngularVelocityComponent> angularVelocities = ComponentMapper.getFor(AngularVelocityComponent.class);
	transient private ComponentMapper<VisionComponent> visions = ComponentMapper.getFor(VisionComponent.class);
	transient private ComponentMapper<Light2dComponent> lights = ComponentMapper.getFor(Light2dComponent.class);
	transient private ComponentMapper<AgentComponent> agents = ComponentMapper.getFor(AgentComponent.class);
	transient private ComponentMapper<SubEntityComponent> subs = ComponentMapper.getFor(SubEntityComponent.class);

	public DizzinessSystem() {
		super(Family.all(DizzinessComponent.class, AngularVelocityComponent.class, VisionComponent.class, Light2dComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
//		DizzinessComponent dizzinessComponent = dizziness.get(entity);
//		AngularVelocityComponent angularVelocityComponent = angularVelocities.get(entity);
//		VisionComponent visionComponent = visions.get(entity);
//		Light2dComponent light2dComponent = lights.get(entity);
//		AgentComponent agentComponent = agents.get(entity);
//		if (Math.abs(angularVelocityComponent.getAngularVelocity()) > 45) {
//			visionComponent.setBlind(true);
//			light2dComponent.setDistance(0);
//			dizzinessComponent.setDizzinessDelay(0.5f);
//			visionComponent.notifyObservers();
//		} else if (dizzinessComponent.getDizzinessDelay() > 0) {
//			dizzinessComponent.setDizzinessDelay(dizzinessComponent.getDizzinessDelay() - deltaTime);
//			if (dizzinessComponent.getDizzinessDelay() <= 0) {
//				visionComponent.setBlind(false);
//				light2dComponent.setDistance(visionComponent.getMaxDistance() * agentComponent.getVisionModifier());
//				visionComponent.notifyObservers();
//			}
//		} else {
//			light2dComponent.setDistance(visionComponent.getMaxDistance() * agentComponent.getVisionModifier());
//			light2dComponent.setAngle(visionComponent.getAngle());
//			SubEntityComponent sub = subs.get(entity);
//			if (sub.size() > 0) {
//				Entity entity1 = sub.get(0);
//				Light2dComponent light2dComponent1 = lights.get(entity1);
//				if (light2dComponent1 != null) {
//					light2dComponent1.setAngle(visionComponent.getAngle());
//				}
//			}
//		}
	}
}
