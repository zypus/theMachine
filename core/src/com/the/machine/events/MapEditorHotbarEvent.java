package com.the.machine.events;

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
public class MapEditorHotbarEvent extends Event {
	int hotbarIndex;
}
