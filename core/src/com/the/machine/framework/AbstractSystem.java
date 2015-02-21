package com.the.machine.framework;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.the.machine.framework.engine.World;
import lombok.Data;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 11/02/15
 */
@Data
public abstract class AbstractSystem
		extends EntitySystem {

	public AbstractSystem(Family family) {
		super(0);
	}

	public AbstractSystem(int priority) {
		super(priority);
	}

	protected World world;

	private boolean renderSystem = false;

}
