package com.the.machine.behaviours;

import com.badlogic.gdx.math.MathUtils;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.systems.ActionSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/03/15
 */
@NoArgsConstructor
public class RandomBehaviour implements BehaviourComponent.Behaviour<RandomBehaviour.RandomBehaviourState> {

	@Override
	public List<BehaviourComponent.BehaviourResponse> evaluate(BehaviourComponent.BehaviourContext context, RandomBehaviourState state) {
		float delta = context.getPastTime();
		state.nextSpeedChange -= delta;
		state.nextTurnChange -= delta;
		List<BehaviourComponent.BehaviourResponse> responses = new ArrayList<>();
		if (state.nextSpeedChange <= 0) {
			responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.MOVE, new ActionSystem.MoveData(MathUtils.random() * 2)));
		}
		if (state.nextTurnChange <= 0) {
			if (context.getSprintTime() > 0) {
				responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.TURN, new ActionSystem.TurnData(MathUtils.random()*360 ,MathUtils.random() * 20 - 10)));
			} else  {
				responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.TURN, new ActionSystem.TurnData(MathUtils.random() * 360, MathUtils.random() * 90 - 45)));
			}
			state.nextTurnChange = nextTime(0.5f)*1;
		}
		if (context.isCanSprint()) {
			if (MathUtils.random() < 0.1) {
				responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.SPRINT, null));
			}
		}
		if (context.getEnvironment() == AreaComponent.AreaType.TOWER && MathUtils.random() < 0.1) {
			responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.TOWER_LEAVE, null));
		}
		responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.TOWER_ENTER, null));
		responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.WINDOW_DESTROY, null));
		if (MathUtils.random() < 0.5) {
			responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.DOOR_OPEN, null));
		} else {
			responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.DOOR_OPEN_SILENT, null));
		}
		if (MathUtils.random() < 0.001) {
			responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.MARKER_PLACE, new ActionSystem.MarkerData(0, 0.5f)));
		}
		return responses;
	}

	private float nextTime(float rate) {
		return (float) (-Math.log(1 - MathUtils.random()) / rate);
	}

	@AllArgsConstructor
	public static class RandomBehaviourState implements BehaviourComponent.BehaviourState {
		float nextSpeedChange;
		float nextTurnChange;
	}

}

