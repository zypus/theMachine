package com.the.machine.events;

import com.badlogic.gdx.math.Vector3;
import com.the.machine.framework.events.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/03/15
 */
@AllArgsConstructor
@Data
public class MarkerEvent extends Event {
	Vector3 location;
	boolean guardMarker;
	int markerNumber;
	float decayRate;
}
