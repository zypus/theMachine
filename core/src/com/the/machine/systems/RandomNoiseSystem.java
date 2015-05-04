package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.MapGroundComponent;
import com.the.machine.components.NoiseMapComponent;
import com.the.machine.events.AudioEvent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.interfaces.Observable;
import com.the.machine.framework.interfaces.Observer;
import lombok.EqualsAndHashCode;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/03/15
 */
@EqualsAndHashCode
public class RandomNoiseSystem
		extends IteratingSystem implements EntityListener, Observer {

	private static final float AREA_LENGTH = 5;
	private static final float RATE = 0.1f;

	transient private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<DimensionComponent> dimensions = ComponentMapper.getFor(DimensionComponent.class);
	transient private ComponentMapper<NoiseMapComponent>  noiseMaps  = ComponentMapper.getFor(NoiseMapComponent.class);

	public RandomNoiseSystem() {
		super(Family.all(MapGroundComponent.class)
					.get());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(Family.all(MapGroundComponent.class)
									   .get(), this);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}

	@Override
	public void entityAdded(Entity entity) {
		DimensionComponent dimensionComponent = dimensions.get(entity);
		float[][] noiseMap = makeNoiseMap(dimensionComponent);
		NoiseMapComponent noiseMapComponent = new NoiseMapComponent();
		noiseMapComponent.setNoiseMap(noiseMap);
		entity.add(noiseMapComponent);
		dimensionComponent.addObserver(this);
	}

	private float[][] makeNoiseMap(DimensionComponent dimensionComponent) {
		float width = dimensionComponent.getWidth();
		float height = dimensionComponent.getHeight();
		int columns = MathUtils.ceil(width / AREA_LENGTH);
		int rows = MathUtils.ceil(height / AREA_LENGTH);
		float[][] noiseMap = new float[columns][rows];
		for (int c = 0; c < columns; c++) {
			for (int r = 0; r < rows; r++) {
				noiseMap[c][r] = nextTime(RATE) * 60; // RATE * 60 seconds
			}
		}
		return noiseMap;
	}

	@Override
	public void entityRemoved(Entity entity) {
		entity.remove(NoiseMapComponent.class);
	}

	@Override
	public void update(Observable o, Object arg) {
		DimensionComponent dimensionComponent = (DimensionComponent) o;
		Entity entity = dimensionComponent.getOwner()
										  .get();
		if (entity != null) {
			float[][] noiseMap = makeNoiseMap(dimensionComponent);
			NoiseMapComponent noiseMapComponent = noiseMaps.get(entity);
			noiseMapComponent.setNoiseMap(noiseMap);
		}
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		DimensionComponent dm = dimensions.get(entity);
		if (noiseMaps.has(entity)) {
			float[][] map = noiseMaps.get(entity)
									 .getNoiseMap();
			for (int c = 0; c < map.length; c++) {
				for (int r = 0; r < map[0].length; r++) {
					map[c][r] -= deltaTime;
					if (map[c][r] <= 0) {
						// compute the location in the area
						Vector3 location = new Vector3(dm.getWidth()/2-AREA_LENGTH * (c + MathUtils.random()), dm.getHeight()/2-AREA_LENGTH * (r + MathUtils.random()), 0);
						location.add(transforms.get(entity)
											   .getPosition());
						world.dispatchEvent(new AudioEvent(location, 5, new WeakReference<>(entity)));
						map[c][r] = nextTime(RATE) * 60;
					}
				}
			}
		}
	}

	private float nextTime(float rate) {
		return (float) (-Math.log(1 - MathUtils.random()) / rate);
	}
}
