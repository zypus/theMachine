package com.the.machine.components;

import com.the.machine.framework.components.AbstractComponent;
import com.the.machine.systems.InputControlledMovementSystem;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ControlComponent extends AbstractComponent {

	Map<Integer, InputControlledMovementSystem.Control> controlMap;

}
