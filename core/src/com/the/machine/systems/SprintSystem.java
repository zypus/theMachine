package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.AngularVelocityComponent;
import com.the.machine.components.SprintComponent;
import com.the.machine.events.SprintEvent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/03/15
 */
public class SprintSystem extends IteratingSystem implements EventListener {

	private static final float SPRINT_MAX_TURN_DEGREE = 10;
	private static final float SPRINT_SPEED = 3;
	private static final float SPRINT_TIME = 5;
	private static final float SPRINT_COOLDOWN = 10;

	transient private ComponentMapper<SprintComponent> sprints = ComponentMapper.getFor(SprintComponent.class);
	transient private ComponentMapper<AgentComponent> agents = ComponentMapper.getFor(AgentComponent.class);
	transient private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<AngularVelocityComponent> angularVelocities = ComponentMapper.getFor(AngularVelocityComponent.class);

	public SprintSystem() {
		super(Family.all(SprintComponent.class, AgentComponent.class, TransformComponent.class)
					.get());
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof SprintEvent) {
			Entity entity = ((SprintEvent) event).getSprinter()
												 .get();
			if (entity != null) {
				SprintComponent sprintComponent = sprints.get(entity);
				AgentComponent agentComponent = agents.get(entity);
				TransformComponent transformComponent = transforms.get(entity);
				if (sprintComponent.getSprintTime() <= 0 && sprintComponent.getSprintCooldown() <= 0) {
					sprintComponent.setSprintStartAngle(transformComponent.getZRotation());
					sprintComponent.setSprintTime(SPRINT_TIME);
					agentComponent.setMaxMovementSpeed(SPRINT_SPEED);
				}
			}
		}
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		SprintComponent sprintComponent = sprints.get(entity);
		AgentComponent agentComponent = agents.get(entity);
		float sprintTime = sprintComponent.getSprintTime();
		if (sprintTime > 0) {
			sprintTime -= deltaTime;
			if (sprintTime <= 0) {
				agentComponent.setMaxMovementSpeed(agentComponent.getBaseMovementSpeed());
				sprintComponent.setSprintCooldown(SPRINT_COOLDOWN);
			} else {
				// limit the turning degree
				TransformComponent tf = transforms.get(entity);
				AngularVelocityComponent av = angularVelocities.get(entity);
				float rotation = tf.getZRotation();
				if (Math.abs(rotation - sprintComponent.getSprintStartAngle()) > SPRINT_MAX_TURN_DEGREE) {
					if (rotation - sprintComponent.getSprintStartAngle() < 0) {
						tf.setZRotation(sprintComponent.getSprintStartAngle() - SPRINT_MAX_TURN_DEGREE);
						av.setAngularVelocity(-44);
					} else {
						tf.setZRotation(sprintComponent.getSprintStartAngle() + SPRINT_MAX_TURN_DEGREE);
						av.setAngularVelocity(44);
					}
				}
			}
			sprintComponent.setSprintTime(sprintTime);
		} else if (sprintComponent.getSprintCooldown() > 0) {
			sprintComponent.setSprintCooldown(sprintComponent.getSprintCooldown() - deltaTime);
		}

	}
}
