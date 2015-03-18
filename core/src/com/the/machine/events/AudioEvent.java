package com.the.machine.events;

import com.badlogic.gdx.math.Vector3;
import com.the.machine.framework.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/03/15
 */
@AllArgsConstructor
@Getter
public class AudioEvent extends Event {
	Vector3 location;
	float hearableDistance;
}
