package com.the.machine.components;

import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SprintComponent extends AbstractComponent {
	float sprintTime = 0;
	float sprintCooldown = 0;
	float sprintStartAngle = 0;
}
