package com.the.machine.framework.events.physics;

import com.badlogic.ashley.core.Entity;
import com.the.machine.framework.events.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/03/15
 */
@Data
@AllArgsConstructor
public class ContactBeginEvent extends Event {
	WeakReference<Entity> first;
	WeakReference<Entity> second;
}
