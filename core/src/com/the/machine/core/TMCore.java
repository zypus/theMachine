package com.the.machine.core;

import com.badlogic.ashley.core.Engine;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 07/02/15
 */
public class TMCore {

	private static TMCore instance = new TMCore();

	private final Engine engine;

	private long lastUpdate;

	private boolean paused;

	private TMCore() {
		// Create the engine
		engine = new Engine();
		paused = true;
	}

	public void start() {
		paused = false;
		lastUpdate = System.currentTimeMillis();
		coreLoop();
	}

	public void setPaused(boolean toggle) {
		if (paused && !toggle) {
			paused = false;
			lastUpdate = System.currentTimeMillis();
			coreLoop();
		} else {
			paused = toggle;
		}
	}

	// TODO: Only start the loop once, pause systems instead
	private void coreLoop() {
		Thread loop = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!paused) {
					// compute the past time since last update
					long time = System.currentTimeMillis();
					float dt = (float) (time - lastUpdate) / 1000f;
					// update the engine
					engine.update(dt);
					// store when the update took place
					lastUpdate = time;
				}
			}
		});
		loop.start();
	}

}
