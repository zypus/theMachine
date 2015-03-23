package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.ListenerComponent;
import com.the.machine.components.MapGroundComponent;
import com.the.machine.components.VisionComponent;
import com.the.machine.events.TowerEnterEvent;
import com.the.machine.events.TowerLeaveEvent;
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

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/03/15
 */
public class TowerSystem
		extends AbstractSystem
		implements EventListener {

	private static final float TOWER_DELAY = 3;
	private static final float TOWER_ANGLE = 30;
	private static final float TOWER_MIN_VIEWING_DISTANCE = 2.5f;
	private static final float TOWER_MAX_VIEWING_DISTANCE = 15.5f;

	transient private ComponentMapper<AreaComponent>      areas      = ComponentMapper.getFor(AreaComponent.class);
	transient private ComponentMapper<DimensionComponent> dimensions = ComponentMapper.getFor(DimensionComponent.class);
	transient private ComponentMapper<VisionComponent>    visions    = ComponentMapper.getFor(VisionComponent.class);
	transient private ComponentMapper<AgentComponent>     agents     = ComponentMapper.getFor(AgentComponent.class);
	transient private ComponentMapper<ListenerComponent> listeners = ComponentMapper.getFor(ListenerComponent.class);
	transient private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);

	transient private ImmutableArray<Entity> mapElements;
	transient private List<TowerCommand> commands = new ArrayList<>();

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
		if (event instanceof TowerEnterEvent) {
			Entity entity = ((TowerEnterEvent) event).getEnterer()
													 .get();
			if (entity != null) {
				AgentComponent agentComponent = agents.get(entity);
				if (agentComponent.getEnvironmentType() == AreaComponent.AreaType.TOWER && !agentComponent.isInTower()) {
					Entity tower = null;
					float dst2 = 10000000f;
					TransformComponent tf = EntityUtilities.computeAbsoluteTransform(entity);
					for (Entity element : mapElements) {
						AreaComponent areaComponent = areas.get(element);
						if (areaComponent.getType() == AreaComponent.AreaType.TOWER) {
							TransformComponent areaTf = EntityUtilities.computeAbsoluteTransform(element);
							float dist = areaTf.getPosition()
											   .dst2(tf.getPosition());
							if (dist < dst2) {
								tower = element;
							}
						}
					}
					if (tower != null) {
						TransformComponent towerTf = EntityUtilities.computeAbsoluteTransform(tower);
//						Vector3 delta = towerTf.getPosition()
//											   .cpy()
//											   .sub(tf.getPosition())
//											   .scl(0.25f);
						commands.add(new TowerCommand(true, TOWER_DELAY, ((TowerEnterEvent) event).getEnterer(), new WeakReference<>(tower), towerTf.getPosition()));
						VisionComponent visionComponent = visions.get(entity);
						visionComponent.setBlind(true);
						agentComponent.setMaxMovementSpeed(0);
						agentComponent.setMaxTurningSpeed(0);
						agentComponent.setActing(true);
						ListenerComponent listenerComponent = listeners.get(entity);
						listenerComponent.setDeaf(true);
					}
				}
			}
		} else if (event instanceof TowerLeaveEvent) {
			Entity entity = ((TowerLeaveEvent) event).getLeaver()
													 .get();
			AgentComponent agentComponent = agents.get(entity);
			if (agentComponent
					  .getEnvironmentType() == AreaComponent.AreaType.TOWER && agentComponent.isInTower()) {
				Entity tower = null;
				float dst2 = 10000000f;
				TransformComponent tf = EntityUtilities.computeAbsoluteTransform(entity);
				for (Entity element : mapElements) {
					AreaComponent areaComponent = areas.get(element);
					if (areaComponent.getType() == AreaComponent.AreaType.TOWER) {
						TransformComponent areaTf = EntityUtilities.computeAbsoluteTransform(element);
						float dist = areaTf.getPosition()
										   .dst2(tf.getPosition());
						if (dist < dst2) {
							dst2 = dist;
							tower = element;
						}
					}
				}
				if (tower != null) {
					TransformComponent towerTf = EntityUtilities.computeAbsoluteTransform(tower);
					DimensionComponent dm = dimensions.get(tower);

					Vector3 delta = tf.getPosition()
										   .cpy()
										   .sub(towerTf.getPosition()).nor()
										   .scl(1.1f*(float) Math.sqrt(Math.pow(dm.getWidth()/2, 2)+Math.pow(dm.getHeight()/2, 2)));
					commands.add(new TowerCommand(false, TOWER_DELAY, ((TowerLeaveEvent) event).getLeaver(), new WeakReference<>(tower), towerTf.getPosition()
																																		   .cpy()
																																		   .add(delta)));
					VisionComponent visionComponent = visions.get(entity);
					visionComponent.setBlind(true);
					agentComponent.setMaxMovementSpeed(0);
					agentComponent.setMaxTurningSpeed(0);
					agentComponent.setActing(true);
					ListenerComponent listenerComponent = listeners.get(entity);
					listenerComponent.setDeaf(true);
				}
			}
		}
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		List<TowerCommand> removeList = new ArrayList<>();
		for (TowerCommand command : commands) {
			command.delay -= deltaTime;
			if (command.delay <= 0) {
				Entity entity = command.getActor()
									   .get();
				VisionComponent visionComponent = visions.get(entity);
				AgentComponent agentComponent = agents.get(entity);
				ListenerComponent listenerComponent = listeners.get(entity);
				TransformComponent tf = transforms.get(entity);
				if (command.entering) {
					visionComponent.setBlind(false);
					visionComponent.setMinDistance(TOWER_MIN_VIEWING_DISTANCE);
					visionComponent.setMaxDistance(TOWER_MAX_VIEWING_DISTANCE);
					visionComponent.setAngle(TOWER_ANGLE);
					agentComponent.setInTower(true);
					agentComponent.setEnvironmentType(AreaComponent.AreaType.TOWER);
				} else {
					visionComponent.setBlind(false);
					visionComponent.setMinDistance(0);
					visionComponent.setMaxDistance(agentComponent.getBaseViewingDistance());
					visionComponent.setAngle(agentComponent.getBaseViewingAngle());
					agentComponent.setInTower(false);
					agentComponent.setEnvironmentType(AreaComponent.AreaType.GROUND);
				}
				visionComponent.notifyObservers();
				agentComponent.setVisionModifier(1f);
				agentComponent.setMaxTurningSpeed(agentComponent.getBaseMaxTurningSpeed());
				agentComponent.setMaxMovementSpeed(agentComponent.getBaseMovementSpeed());
				agentComponent.setActing(false);

				listenerComponent.setDeaf(false);
				tf.setPosition(command.accessPoint);
				tf.notifyObservers();
				removeList.add(command);
			}
		}
		for (TowerCommand command : removeList) {
			commands.remove(command);
		}
	}

	@AllArgsConstructor
	@Data
	private static class TowerCommand {
		boolean               entering;
		float                 delay;
		WeakReference<Entity> actor;
		WeakReference<Entity> tower;
		Vector3 accessPoint;
	}
}
