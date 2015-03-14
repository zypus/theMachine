package com.the.machine.framework;

import com.badlogic.ashley.core.EntitySystem;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.events.Event;
import lombok.Data;

/**
 * All Systems extend this AbstractSystem class
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 11/02/15
 */
@Data
public abstract class AbstractSystem
		extends EntitySystem {

	public AbstractSystem() {
		super(0);
	}

	public AbstractSystem(int priority) {
		super(priority);
	}

	private Class<? extends Event>[] events = null;

	transient protected World world;

	private boolean renderSystem = false;
	private boolean isStable = false;
	private boolean active = true;

}
