package com.the.machine.systems;

import com.the.machine.components.BehaviourComponent;
import com.the.machine.framework.AbstractSystem;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/03/15
 */
public class ActionSystem extends AbstractSystem {

	public static enum Action {
		WINDOW_DESTROY,
		DOOR_OPEN,
		DOOR_OPEN_SILENT,
		DOOR_CLOSE,
		DOOR_CANCEL,
		TOWER_ENTER,
		TOWER_LEAVE,
		SPRINT,
		MARKER_PLACE,
		STATE,
		TURN,
		MOVE
	}

	@Data
	@AllArgsConstructor
	public static class MarkerData {
		int number;
		float decay;
	}

	@Data
	@AllArgsConstructor
	public static class TurnData {
		float angle;
		float speed;
	}

	@Data
	@AllArgsConstructor
	public static class MoveData {
		float speed;
	}

	@Data
	@AllArgsConstructor
	public static class StateData {
		BehaviourComponent.BehaviourState state;
	}
}
