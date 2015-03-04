package com.the.machine.components;

import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 25/02/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class VelocityComponent extends AbstractComponent {
	float velocity = 0;
}
