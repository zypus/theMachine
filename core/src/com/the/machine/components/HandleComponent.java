package com.the.machine.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 04/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class HandleComponent extends AbstractComponent {

	private Vector3 referencePosition = new Vector3();
	private Rectangle rect = new Rectangle();
	private HandleType type;
	private Entity uiCamera;
	private Entity worldCamera;

	public static enum HandleType {
		TOP_LEFT,
		TOP,
		TOP_RIGHT,
		RIGHT,
		BOTTOM_RIGHT,
		BOTTOM,
		BOTTOM_LEFT,
		LEFT
	}

}
