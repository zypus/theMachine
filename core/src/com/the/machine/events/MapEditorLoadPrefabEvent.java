package com.the.machine.events;

import com.the.machine.framework.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 15/03/15
 */
@AllArgsConstructor
@Getter
public class MapEditorLoadPrefabEvent extends Event {
	String name;
}
