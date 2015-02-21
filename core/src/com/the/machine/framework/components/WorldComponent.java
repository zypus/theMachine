package com.the.machine.framework.components;

import com.the.machine.framework.engine.World;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 11/02/15
 */
@Data
@Accessors(chain = true)
public class WorldComponent extends AbstractComponent {
	private World world;
}
