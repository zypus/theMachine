package com.the.machine.events;

import com.badlogic.ashley.core.Entity;
import com.the.machine.framework.events.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/03/15
 */
@AllArgsConstructor
@Data
public class TowerEnterEvent extends Event {
	WeakReference<Entity> enterer;
}
