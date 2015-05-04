package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.AngularVelocityComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.DiscreteMapComponent;
import com.the.machine.components.ListenerComponent;
import com.the.machine.components.SprintComponent;
import com.the.machine.components.VelocityComponent;
import com.the.machine.components.VisionComponent;
import com.the.machine.events.DoorCancelEvent;
import com.the.machine.events.DoorOpenEvent;
import com.the.machine.events.MarkerEvent;
import com.the.machine.events.SprintEvent;
import com.the.machine.events.TowerEnterEvent;
import com.the.machine.events.TowerLeaveEvent;
import com.the.machine.events.WindowDestroyEvent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.misc.Placebo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frans on 12-3-2015.
 */
public class BehaviourSystem
		extends IteratingSystem {
	private transient ComponentMapper<BehaviourComponent>       behaviours        = ComponentMapper.getFor(BehaviourComponent.class);
	private transient ComponentMapper<TransformComponent>       transforms        = ComponentMapper.getFor(TransformComponent.class);
	private transient ComponentMapper<VelocityComponent>        velocities        = ComponentMapper.getFor(VelocityComponent.class);
	private transient ComponentMapper<AngularVelocityComponent> angularVelocities = ComponentMapper.getFor(AngularVelocityComponent.class);
	private transient ComponentMapper<AgentComponent>           agents            = ComponentMapper.getFor(AgentComponent.class);
	private transient ComponentMapper<ListenerComponent>        listeners         = ComponentMapper.getFor(ListenerComponent.class);
	private transient ComponentMapper<VisionComponent>          visions           = ComponentMapper.getFor(VisionComponent.class);
	private transient ComponentMapper<SprintComponent>          sprints           = ComponentMapper.getFor(SprintComponent.class);
	private transient ComponentMapper<DiscreteMapComponent>          discretes           = ComponentMapper.getFor(DiscreteMapComponent.class);

	public BehaviourSystem() {
		super(Family.all(VelocityComponent.class, AngularVelocityComponent.class, AgentComponent.class, ListenerComponent.class, VisionComponent.class, BehaviourComponent.class)
					.get());
	}

	ImmutableArray<Entity> maps;

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		maps = engine.getEntitiesFor(Family.all(DiscreteMapComponent.class)
										   .get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		maps = null;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		VelocityComponent velocityComponent = velocities.get(entity);
		float velocity = velocityComponent
				.getVelocity();
		AngularVelocityComponent angularVelocityComponent = angularVelocities.get(entity);
		float angularVelocity = angularVelocityComponent
				.getAngularVelocity();
		VisionComponent visionComponent = visions.get(entity);
		List<DiscreteMapComponent.MapCell> cells = visionComponent.getVisibleCells();
		List<WeakReference<Entity>> visibleAgents = visionComponent.getVisibleAgents();
		List<WeakReference<Entity>> visibleMarkers = visionComponent.getVisibleMarkers();
		ListenerComponent listenerComponent = listeners.get(entity);
		List<Vector2> directions;
		if (listenerComponent.isDeaf()) {
			directions = new ArrayList<>();
		} else {
			directions = listenerComponent
					.getSoundDirections();
		}
		AgentComponent agentComponent = agents.get(entity);

		// sprinting
		boolean canSprint = false;
		float sprintTime = 0;
		float sprintCooldown = 0;
		if (sprints.has(entity)) {
			SprintComponent sprintComponent = sprints.get(entity);
			canSprint = true;
			sprintTime = sprintComponent.getSprintTime();
			sprintCooldown = sprintComponent.getSprintCooldown();
		}

		// direction
		TransformComponent tf = transforms.get(entity);
		Vector3 dir = new Vector3(1, 0, 0);
		tf.getRotation()
		  .transform(dir);

		Placebo placebo = new Placebo(tf.get2DPosition(), discretes.get(maps.first())
																   .getSparseMap());

		// context
		BehaviourComponent.BehaviourContext context = new BehaviourComponent.BehaviourContext(deltaTime, velocity, angularVelocity, new Vector2(dir.x, dir.y), agentComponent.getEnvironmentType(), cells, visibleAgents, visibleMarkers, directions, canSprint, sprintTime, sprintCooldown, agentComponent.isHidden(), agentComponent.isInTower(), agentComponent.getVisionModifier()*agentComponent.getBaseViewingDistance(), agentComponent.getViewingAngle(), placebo);

		BehaviourComponent behaviourComponent = behaviours.get(entity);

		// evaluate the behaviour
		BehaviourComponent.BehaviourResponse response = behaviourComponent.getBehaviour()
																		  .evaluate(context, behaviourComponent.getState());

		// set the new velocities, but keep them in their bound
		if (agentComponent.isActing()) {
			velocityComponent.setVelocity(0);
		} else {
			if (sprintTime > 0) {
				velocityComponent.setVelocity(MathUtils.clamp(response.getMovementSpeed(), agentComponent.getMaxMovementSpeed(), agentComponent.getMaxMovementSpeed()));
			} else {
				velocityComponent.setVelocity(MathUtils.clamp(response.getMovementSpeed(), 0, agentComponent.getMaxMovementSpeed()));
			}
		}
		angularVelocityComponent.setAngularVelocity(MathUtils.clamp(response.getTurningSpeed(), -agentComponent.getMaxTurningSpeed(), agentComponent.getMaxTurningSpeed()));

		WeakReference<Entity> weakReference = new WeakReference<>(entity);
		for (Object o : response.getActions()) {
			ActionSystem.Action action = (ActionSystem.Action) o;
			if (!agentComponent.isActing()) {
				if (action == ActionSystem.Action.SPRINT) {
					world.dispatchEvent(new SprintEvent(weakReference));
				} else if (action == ActionSystem.Action.TOWER_ENTER) {
					world.dispatchEvent(new TowerEnterEvent(weakReference));
				} else if (action == ActionSystem.Action.TOWER_LEAVE) {
					world.dispatchEvent(new TowerLeaveEvent(weakReference));
				} else if (action == ActionSystem.Action.DOOR_OPEN) {
					world.dispatchEvent(new DoorOpenEvent(weakReference, false));
				} else if (action == ActionSystem.Action.DOOR_OPEN_SILENT) {
					world.dispatchEvent(new DoorOpenEvent(weakReference, true));
				} else if (action == ActionSystem.Action.WINDOW_DESTROY) {
					world.dispatchEvent(new WindowDestroyEvent(weakReference));
				} else if (action == ActionSystem.Action.MARKER_PLACE && !agentComponent.isInTower()) {
					world.dispatchEvent(new MarkerEvent(tf.getPosition(), !sprints.has(entity), response.getMarkerNumber(), response.getDecayRate()));
				}
				// TODO perform the action
			} else {
				if (action == ActionSystem.Action.DOOR_CANCEL) {
					world.dispatchEvent(new DoorCancelEvent(weakReference));
				}
			}
		}

		listenerComponent.getSoundDirections()
						 .clear();

		behaviourComponent.setState(response.getNextBehaviourState());
	}
}
