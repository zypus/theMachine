package com.the.machine.framework.events.input;

import com.the.machine.framework.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/03/15
 */
@Getter
@AllArgsConstructor
public class TouchDownEvent
		extends Event {
	int screenX;
	int screenY;
	int pointer;
	int button;
}
