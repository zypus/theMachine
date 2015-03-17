package com.the.machine.framework.components.physics;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 07/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Physics2dComponent
		extends AbstractComponent {
		transient Body body;
		BodyDef.BodyType type = BodyDef.BodyType.DynamicBody;
}
