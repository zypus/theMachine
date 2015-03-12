package com.the.machine.framework.components.canvasElements;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/02/15
 */
@Data
@Accessors(chain = true)
public class CanvasElementComponent extends AbstractComponent {
	transient private Actor   actor = null;
	transient private boolean group = false;
	transient private boolean added = false;
	transient private Actor unwrappedActor = null;
	transient private boolean enableTransform = false;
	private List<EventListener> listeners = new ArrayList<>();
	private Touchable touchable = Touchable.enabled;

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
