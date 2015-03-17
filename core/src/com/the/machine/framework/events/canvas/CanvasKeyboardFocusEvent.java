package com.the.machine.framework.events.canvas;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.the.machine.framework.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 15/03/15
 */
@Getter
@AllArgsConstructor
public class CanvasKeyboardFocusEvent
		extends Event {
	Actor actor;
}
