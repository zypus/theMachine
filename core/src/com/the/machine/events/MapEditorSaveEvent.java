package com.the.machine.events;

import com.the.machine.framework.events.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 07/03/15
 */
@AllArgsConstructor
@Data
public class MapEditorSaveEvent extends Event {
	String name;
}
