package com.the.machine.framework;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 14/02/15
 */
public abstract class IntervalIteratingSystem extends IntervalSystem {
	private Family                 family;
	private ImmutableArray<Entity> entities;

	/**
	 * @param family
	 * 		represents the collection of family the system should process
	 * @param interval
	 * 		time in seconds between calls to {@link IntervalIteratingSystem#updateInterval()}.
	 */
	public IntervalIteratingSystem(Family family, float interval) {
		this(family, interval, 0);
	}

	/**
	 * @param family
	 * 		represents the collection of family the system should process
	 * @param interval
	 * 		time in seconds between calls to {@link IntervalIteratingSystem#updateInterval()}.
	 * @param priority
	 */
	public IntervalIteratingSystem (Family family, float interval, int priority) {
		super(interval, priority);
		this.family = family;
	}

	@Override
	public void addedToEngine (Engine engine) {
		entities = engine.getEntitiesFor(family);
	}

	@Override
	protected void updateInterval () {
		for (int i = 0; i < entities.size(); ++i) {
			processEntity(entities.get(i));
		}
	}

	/**
	 * @return set of entities processed by the system
	 */
	public ImmutableArray<Entity> getEntities () {
		return entities;
	}

	/**
	 * The user should place the entity processing logic here.
	 *
	 * @param entity
	 */
	protected abstract void processEntity (Entity entity);
}
