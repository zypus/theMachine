package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.DiscreteMapComponent;
import com.the.machine.components.MarkerComponent;
import com.the.machine.components.VisionComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.physics.Light2dComponent;
import com.the.machine.framework.utility.EntityUtilities;
import lombok.EqualsAndHashCode;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/03/15
 */
@EqualsAndHashCode
public class VisionSystem
		extends IteratingSystem {

	transient private ComponentMapper<DiscreteMapComponent> discreteMaps = ComponentMapper.getFor(DiscreteMapComponent.class);
	transient private ComponentMapper<TransformComponent>   transforms   = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<DimensionComponent>   dimensions   = ComponentMapper.getFor(DimensionComponent.class);
	transient private ComponentMapper<VisionComponent>      visions      = ComponentMapper.getFor(VisionComponent.class);
	transient private ComponentMapper<AgentComponent> agents = ComponentMapper.getFor(AgentComponent.class);
	transient private ComponentMapper<Light2dComponent> lights = ComponentMapper.getFor(Light2dComponent.class);
	transient private ComponentMapper<ParentComponent> parents = ComponentMapper.getFor(ParentComponent.class);
	private ImmutableArray<Entity> mapEntities;
	private ImmutableArray<Entity> agentEntities;
	private ImmutableArray<Entity> markerEntities;

	public VisionSystem() {
		super(Family.all(Light2dComponent.class)
					.exclude(AgentComponent.class)
					.get());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		mapEntities = world.getEntitiesFor(Family.all(DiscreteMapComponent.class)
												 .get());
		agentEntities = world.getEntitiesFor(Family.all(AgentComponent.class)
												   .get());
		markerEntities = world.getEntitiesFor(Family.all(MarkerComponent.class)
												   .get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		mapEntities = null;
		agentEntities = null;
		markerEntities = null;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		// TODO line of sight
		Entity agent = parents.get(entity)
								.getParent()
								.get();
		if (agent != null) {
			VisionComponent visionComponent = visions.get(agent);
			List<DiscreteMapComponent.MapCell> cells = visionComponent.getVisibleCells();
			List<WeakReference<Entity>> visibleAgents = visionComponent.getVisibleAgents();
			List<WeakReference<Entity>> visibleMarkers = visionComponent.getVisibleMarkers();
			visibleAgents.clear();
			cells.clear();
			if (mapEntities.size() > 0 && !visions.get(agent)
												  .isBlind()) {
				Entity mapEntity = mapEntities.first();
				DimensionComponent dimensionComponent = dimensions.get(mapEntity);
				TransformComponent transformComponent = transforms.get(mapEntity);
				DiscreteMapComponent discreteMapComponent = discreteMaps.get(mapEntity);
				List<DiscreteMapComponent.MapCell> map = discreteMapComponent.getSparseMap();

				AgentComponent agentComponent = agents.get(agent);
				float min = visionComponent.getMinDistance() * agentComponent.getVisionModifier();
				min *= min;
				float max = visionComponent.getMaxDistance() * agentComponent.getVisionModifier();
				max *= max;
				Light2dComponent light2dComponent = lights.get(entity);
				for (DiscreteMapComponent.MapCell cell : map) {
					Vector2 cellPos = cell.getPosition()
										  .cpy()
										  .scl(0.1f);
					boolean contains = light2dComponent.getLight()
													   .contains(cellPos.x, cellPos.y);
					boolean outerWall = false;
					if (cell.getType() == AreaComponent.AreaType.OUTER_WALL) {
						float angle = visionComponent.getAngle();
						TransformComponent tf = EntityUtilities.computeAbsoluteTransform(agent);
						float rotation = normAngle(tf
														   .getZRotation());
						Vector2 delta = cell.getPosition().cpy().sub(tf.get2DPosition());
						float deltaAngle = normAngle(delta.angle());
						outerWall = Math.abs(rotation - deltaAngle) < (angle / 2);
					}
					if (contains || outerWall) {
						TransformComponent tf = EntityUtilities.computeAbsoluteTransform(agent);
						Vector2 delta = cell.getPosition()
											.cpy()
											.sub(tf.get2DPosition());
						float dst2 = delta.len2();
						if (dst2 <= max && dst2 >= min) {
							if (cell.getType() != AreaComponent.AreaType.COVER || dst2 <= max/4) {
									cells.add(new DiscreteMapComponent.MapCell(cell));
							}
						} else if (cell.getType()
									   .isStructure() && dst2 <= 100) {
							cells.add(new DiscreteMapComponent.MapCell(cell));
						} else if (cell.getType()
									   .isTower()) {
							DiscreteMapComponent.MapCell tower = new DiscreteMapComponent.MapCell(cell);
							tower.setType(AreaComponent.AreaType.TOWER);
							cells.add(cell);
						}
					}
				}
			}
			if (agentEntities.size() > 0 && !visions.get(agent)
													.isBlind()) {
				AgentComponent agentComponent = agents.get(agent);
				float min = visionComponent.getMinDistance() * agentComponent.getVisionModifier();
				min *= min;
				float max = visionComponent.getMaxDistance() * agentComponent.getVisionModifier();
				max *= max;
				float angle = visionComponent.getAngle();
				for (Entity aEntity : agentEntities) {
					TransformComponent tf = EntityUtilities.computeAbsoluteTransform(agent);
					TransformComponent atf = EntityUtilities.computeAbsoluteTransform(aEntity);
					AgentComponent aComponent = agents.get(aEntity);
					float rotation = normAngle(tf.getZRotation());
					Vector2 delta = atf.get2DPosition()
									   .cpy()
									   .sub(tf.get2DPosition());
					float dst2 = delta.len2();
					float deltaAngle = normAngle(delta.angle());
					if (dst2 <= max && dst2 >= min && Math.abs(rotation - deltaAngle) < (angle / 2)) {
						if (aComponent.getEnvironmentType() != AreaComponent.AreaType.COVER || dst2 <= max/4) {
							visibleAgents.add(new WeakReference<>(aEntity));
						}
					}
				}
			}
			if (markerEntities.size() > 0 && !visions.get(agent)
													.isBlind()) {
				AgentComponent agentComponent = agents.get(agent);
				float min = visionComponent.getMinDistance() * agentComponent.getVisionModifier();
				min *= min;
				float max = visionComponent.getMaxDistance() * agentComponent.getVisionModifier();
				max *= max;
				float angle = visionComponent.getAngle();
				for (Entity aEntity : markerEntities) {
					TransformComponent tf = EntityUtilities.computeAbsoluteTransform(agent);
					TransformComponent atf = EntityUtilities.computeAbsoluteTransform(aEntity);
//					AgentComponent aComponent = agents.get(aEntity);
					float rotation = normAngle(tf.getZRotation());
					Vector2 delta = atf.get2DPosition()
									   .cpy()
									   .sub(tf.get2DPosition());
					float dst2 = delta.len2();
					float deltaAngle = normAngle(delta.angle());
					if (dst2 <= max && dst2 >= min && Math.abs(rotation - deltaAngle) < (angle / 2)) {
//						if (aComponent.getEnvironmentType() != AreaComponent.AreaType.COVER || dst2 <= max / 4) {
							visibleMarkers.add(new WeakReference<>(aEntity));
//						}
					}
				}
			}
		}
	}

	private float normAngle(float angle) {
		return (angle%360);
	}
}
