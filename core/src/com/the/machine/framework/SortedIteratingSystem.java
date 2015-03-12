package com.the.machine.framework;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * 1@created 14/02/15
 */
public abstract class SortedIteratingSystem extends AbstractSystem implements EntityListener {
	transient protected        Family                 family;
	transient private       Array<Entity>          sortedEntities;
	transient private final ImmutableArray<Entity> entities;
	transient private       boolean                shouldSort;
	transient @Getter @Setter private Comparator<Entity> comparator;

	public SortedIteratingSystem(Family family) {
		this(family, (o1, o2) -> 0, 0);
	}

	/**
	 * Instantiates a system that will iterate over the entities described by the Family.
	 *
	 * @param family
	 * 		The family of entities iterated over in this System
	 * @param comparator
	 * 		The comparator to sort the entities
	 */
	public SortedIteratingSystem(Family family, Comparator<Entity> comparator) {
		this(family, comparator, 0);
	}

	/**
	 * Instantiates a system that will iterate over the entities described by the Family, with a specific priority.
	 *
	 * @param family
	 * 		The family of entities iterated over in this System
	 * @param comparator
	 * 		The comparator to sort the entities
	 * @param priority
	 * 		The priority to execute this system with (lower means higher priority)
	 */
	public SortedIteratingSystem(Family family, Comparator<Entity> comparator, int priority) {
		super(priority);

		this.family = family;
		sortedEntities = new Array<Entity>(false, 16);
		entities = new ImmutableArray<Entity>(sortedEntities);
		this.comparator = comparator;
	}

	/**
	 * Call this if the sorting criteria have changed. The actual sorting will be delayed until the entities are processed.
	 */
	public void forceSort() {
		shouldSort = true;
	}

	private void sort() {
		if (shouldSort) {
			sortedEntities.sort(comparator);
			shouldSort = false;
		}
	}

	@Override
	public void addedToEngine(Engine engine) {
		ImmutableArray<Entity> newEntities = engine.getEntitiesFor(family);
		sortedEntities.clear();
		if (newEntities.size() > 0) {
			for (int i = 0; i < newEntities.size(); ++i) {
				sortedEntities.add(newEntities.get(i));
			}
			sortedEntities.sort(comparator);
		}
		shouldSort = false;
		engine.addEntityListener(family, this);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		engine.removeEntityListener(this);
		sortedEntities.clear();
		shouldSort = false;
	}

	@Override
	public void entityAdded(Entity entity) {
		sortedEntities.add(entity);
		shouldSort = true;
	}

	@Override
	public void entityRemoved(Entity entity) {
		sortedEntities.removeValue(entity, true);
	}

	@Override
	public void update(float deltaTime) {
		sort();
		for (int i = 0; i < sortedEntities.size; ++i) {
			processEntity(sortedEntities.get(i), deltaTime);
		}
	}

	/**
	 * @return set of entities processed by the system
	 */
	public ImmutableArray<Entity> getEntities() {
		sort();
		return entities;
	}

	/**
	 * This method is called on every entity on every update call of the EntitySystem. Override this to implement your system's
	 * specific processing.
	 *
	 * @param entity
	 * 		The current Entity being processed
	 * @param deltaTime
	 * 		The delta time between the last and current frame
	 */
	protected abstract void processEntity(Entity entity, float deltaTime);
}
