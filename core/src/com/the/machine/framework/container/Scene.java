package com.the.machine.framework.container;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Bits;
import com.the.machine.framework.AbstractSystem;
import lombok.Data;


/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 16/02/15
 */
@Data
public class Scene {
	private String name;
	private ImmutableArray<Bits>           layers;
	private ImmutableArray<Entity>         entities;
	private ImmutableArray<AbstractSystem> systems;
}
