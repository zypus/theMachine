package com.the.machine.framework;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 14/02/15
 */
public abstract class IntervalSystem
		extends AbstractSystem {
	private float interval;
	private float accumulator;

	/**
	 * @param interval
	 * 		time in seconds between calls to {@link IntervalSystem#updateInterval()}.
	 */
	public IntervalSystem(float interval) {
		this(interval, 0);
	}

	/**
	 * @param interval
	 * 		time in seconds between calls to {@link IntervalSystem#updateInterval()}.
	 * @param priority
	 */
	public IntervalSystem(float interval, int priority) {
		super(priority);
		this.interval = interval;
		this.accumulator = 0;
	}

	@Override
	public void update (float deltaTime) {
		accumulator += deltaTime;

		if (accumulator >= interval) {
			accumulator -= interval;
			updateInterval();
		}
	}

	/**
	 * The processing logic of the system should be placed here.
	 */
	protected abstract void updateInterval ();
}
