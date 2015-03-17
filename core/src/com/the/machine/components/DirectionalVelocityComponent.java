package com.the.machine.components;

import com.badlogic.gdx.math.Vector3;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DirectionalVelocityComponent extends AbstractComponent {
	Vector3 velocity = new Vector3();
}
