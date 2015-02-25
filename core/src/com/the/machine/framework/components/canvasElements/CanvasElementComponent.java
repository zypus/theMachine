package com.the.machine.framework.components.canvasElements;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/02/15
 */
@Data
public class CanvasElementComponent extends AbstractComponent {
	transient private Actor   actor = null;
	transient private boolean group = false;
	transient private boolean added = false;
	transient private Actor unwrappedActor = null;
	transient private boolean enableTransform = false;

	public Group getGroup() {
		if (group) {
			return (Group) getUnwrappedActor();
		} else {
			return null;
		}
	}

	public Actor getUnwrappedActor() {
		if (isWrapped()) {
			return unwrappedActor;
		} else {
			return actor;
		}
	}

	public boolean isWrapped() {
		return unwrappedActor != null;
	}

	public boolean isSet() {
		return actor != null;
	}
}
