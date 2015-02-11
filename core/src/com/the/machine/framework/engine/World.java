package com.the.machine.framework.engine;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventEngine;
import com.the.machine.framework.events.EventListener;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created $(DATE)
 *
 * @startuml
 *
 * a <|-- b
 * b --* c
 * a --o c
 *
 * @enduml
 */
public class World implements Runnable {

	private EventEngine  eventEngine;
	private Engine       engine;
	private TweenManager tweenManager;

	private WorldState worldState;

	private long lastUpdate;

	public World() {
		eventEngine = new EventEngine();
		engine = new Engine();
		tweenManager = new TweenManager();
		worldState = WorldState.INITIALIZED;
	}

	public void startWorld() {
		if (worldState == WorldState.INITIALIZED) {
			lastUpdate = System.currentTimeMillis();
			Thread worldThread = new Thread(this);
			worldState = WorldState.RUNNING;
			worldThread.start();
		} else {
			System.out.println("World is already started!");
		}
	}

	@Override
	public void run() {

		float delta = 0;
		while (!Thread.currentThread()
					  .isInterrupted()) {
			if (worldState.equals(WorldState.RUNNING)) {
				// delta time calculation
				long now = System.currentTimeMillis();
				delta = (now - lastUpdate) / 1000f;
				lastUpdate = now;

				eventEngine.update();
				tweenManager.update(delta);
				engine.update(delta);
			} else {
				// In order to prevent the loop of going mad, we sleep for a little time.
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**===============================================================================================================================**/

	/**
	 * Engine delegation methods
	 */

	public void pause() {
		worldState = WorldState.PAUSED;
	}

	public void unpause() {
		lastUpdate = System.currentTimeMillis();
		worldState = WorldState.RUNNING;
	}

	public void addEntity(Entity entity) {
		engine.addEntity(entity);
	}

	public void addEntityListener(EntityListener listener) {
		engine.addEntityListener(listener);
	}

	public <T extends EntitySystem> T getSystem(Class<T> systemType) {
		return engine.getSystem(systemType);
	}

	public void removeAllEntities() {
		engine.removeAllEntities();
	}

	public void removeEntity(Entity entity) {
		engine.removeEntity(entity);
	}

	public void removeSystem(EntitySystem system) {
		engine.removeSystem(system);
	}

	public void removeEntityListener(EntityListener listener) {
		engine.removeEntityListener(listener);
	}

	public void update(float deltaTime) {
		engine.update(deltaTime);
	}

	public ImmutableArray<Entity> getEntitiesFor(Family family) {
		return engine.getEntitiesFor(family);
	}

	public void addSystem(EntitySystem system) {
		engine.addSystem(system);
	}

	/**-------------------------------------------------------------------------------------------------------------------------------**/

	/**
	 * Event engine delegation methods
	 */

	@SafeVarargs public final void register(EventListener registrant,
			Class<? extends Event>... events) {
		eventEngine.register(registrant, events);
	}

	public void unregister(EventListener unregistrant) {
		eventEngine.unregister(unregistrant);
	}

	@SafeVarargs public final void unregister(EventListener unregistrant,
			Class<? extends Event>... events) {
		eventEngine.unregister(unregistrant, events);
	}

	public void dispatchEvent(Event event) {
		eventEngine.dispatchEvent(event);
	}

	/**-------------------------------------------------------------------------------------------------------------------------------**/

	/**
	 * Tween engine methods
	 */

	public void startTimeline(Timeline timeline) {
		timeline.start(tweenManager);
	}

}
