package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.DiscreteMapComponent;
import com.the.machine.components.MapGroundComponent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.interfaces.Observable;
import com.the.machine.framework.interfaces.Observer;
import com.the.machine.framework.utility.Utils;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/03/15
 */
@EqualsAndHashCode
public class DiscretizedMapSystem extends AbstractSystem implements EntityListener, Observer {

	transient private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<DimensionComponent> dimensions = ComponentMapper.getFor(DimensionComponent.class);
	transient private ComponentMapper<AreaComponent> areas = ComponentMapper.getFor(AreaComponent.class);
	transient private ComponentMapper<DiscreteMapComponent> discreteMaps = ComponentMapper.getFor(DiscreteMapComponent.class);

	transient private ImmutableArray<Entity> groundEntities;
	transient private ImmutableArray<Entity> elements;

	transient boolean recomputeMap = true;

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		groundEntities = world.getEntitiesFor(Family.all(MapGroundComponent.class)
													.get());
		Family elementFamily = Family.all(AreaComponent.class, TransformComponent.class, DimensionComponent.class).exclude(MapGroundComponent.class)
									 .get();
		elements = world.getEntitiesFor(elementFamily);
		engine.addEntityListener(elementFamily, this);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		groundEntities = null;
		elements = null;
		engine.removeEntityListener(this);
	}

	@Override
	public void entityAdded(Entity entity) {
		recomputeMap = true;
		transforms.get(entity).addObserver(this);
		dimensions.get(entity).addObserver(this);
		areas.get(entity).addObserver(this);
	}

	@Override
	public void entityRemoved(Entity entity) {
		recomputeMap = true;
		transforms.get(entity)
				  .deleteObserver(this);
		dimensions.get(entity)
				  .deleteObserver(this);
		areas.get(entity)
			 .deleteObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		recomputeMap = true;
	}

	@Override
	public void setProcessing(boolean processing) {
		super.setProcessing(processing);
		if (processing) {
			recomputeMap = true;
		}
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (groundEntities.size() > 0 && recomputeMap) {
			Entity map = groundEntities.first();
			DimensionComponent mapDimension = dimensions.get(map);
			List<DiscreteMapComponent.MapCell> discreteMap;
			if (!discreteMaps.has(map)) {
				DiscreteMapComponent mapComponent = new DiscreteMapComponent();
				discreteMap = mapComponent.getSparseMap();
				map.add(mapComponent);
			} else {
				discreteMap = discreteMaps.get(map).getSparseMap();
			}
			discreteMap.clear();
			float mx = (mapDimension.getWidth()/2);
			float my = (mapDimension.getHeight()/2);
			for (Entity element : elements) {
				TransformComponent tf = transforms.get(element);
				DimensionComponent dm = dimensions.get(element);
				float lx = -dm.getWidth()/2 + tf.getX();
				float ly = -dm.getHeight()/2 + tf.getY();
				AreaComponent areaComponent = areas.get(element);
				for (int x = 0; x <= (int)dm.getWidth(); x++) {
					for (int y = 0; y <= (int)dm.getHeight(); y++) {
						if ((areaComponent.getType() != AreaComponent.AreaType.WALL && areaComponent.getType() != AreaComponent.AreaType.DOOR_CLOSED) || x == 0 || x == (int)dm.getWidth() || y == 0 || y == (int) dm.getHeight()) {
							float c = lx + x;
							float r = ly + y;
							if (Utils.isInbound(c, r, -mapDimension.getWidth() / 2 - 1, -mapDimension.getHeight() / 2 - 1, mapDimension.getWidth(), mapDimension.getHeight())) {
								discreteMap.add(new DiscreteMapComponent.MapCell().setPosition(new Vector2(c, r))
																				  .setType(areaComponent
																								   .getType()));
							}
						}
					}
				}
			}
			// set the outer wall
			for (int x = 1; x < mapDimension.getWidth()+1; x++) {
				discreteMap.add(new DiscreteMapComponent.MapCell().setPosition(new Vector2(x-mx, -my-1))
																  .setType(AreaComponent.AreaType.OUTER_WALL));
				discreteMap.add(new DiscreteMapComponent.MapCell().setPosition(new Vector2(x-mx, my+1))
																  .setType(AreaComponent.AreaType.OUTER_WALL));
			}
			for (int y = 0; y < mapDimension.getHeight(); y++) {
				discreteMap.add(new DiscreteMapComponent.MapCell().setPosition(new Vector2(-mx-1, y-my))
																  .setType(AreaComponent.AreaType.OUTER_WALL));
				discreteMap.add(new DiscreteMapComponent.MapCell().setPosition(new Vector2(mx + 1, y-my))
																  .setType(AreaComponent.AreaType.OUTER_WALL));
			}

			recomputeMap = false;
		}
	}

	private DiscreteMapComponent.MapCell[][] makeMap(int width, int height) {
		DiscreteMapComponent.MapCell[][] discreteMap = new DiscreteMapComponent.MapCell[width][height];
		for (int c = 0; c < width; c++) {
			for (int r = 0; r < height; r++) {
				discreteMap[c][r] = new DiscreteMapComponent.MapCell();
			}
		}
		return discreteMap;
	}
}
