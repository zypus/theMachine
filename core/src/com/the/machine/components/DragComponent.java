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
 * @created 14/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DragComponent extends AbstractComponent {
	transient Vector3 dragPoint        = new Vector3();
	transient Vector3 deltaToDragPoint = new Vector3();
}
