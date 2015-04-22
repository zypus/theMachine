package com.the.machine.behaviours;

import com.badlogic.gdx.math.MathUtils;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.systems.ActionSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/03/15
 */
@NoArgsConstructor
public class RandomBehaviour implements BehaviourComponent.Behaviour<RandomBehaviour.RandomBehaviourState> {

	@Override
	public BehaviourComponent.BehaviourResponse<RandomBehaviourState> evaluate(BehaviourComponent.BehaviourContext context, RandomBehaviourState state) {
		float delta = context.getPastTime();
		state.nextSpeedChange -= delta;
		state.nextTurnChange -= delta;
		BehaviourComponent.BehaviourResponse<RandomBehaviourState> response = new BehaviourComponent.BehaviourResponse<>(context.getCurrentMovementSpeed(), context.getCurrentTurningSpeed(), new ArrayList<>(), state, 0, 0);
		if (state.nextSpeedChange <= 0) {
			response.setMovementSpeed(MathUtils.random()*2);
			state.nextSpeedChange = nextTime(0.5f)*10;
		}
		if (state.nextTurnChange <= 0) {
			if (context.getSprintTime() > 0) {
				response.setTurningSpeed(MathUtils.random() * 20 - 10);
			} else  {
				response.setTurningSpeed(MathUtils.random() * 90 - 45);
			}
			state.nextTurnChange = nextTime(0.5f)*1;
		}
		if (context.isCanSprint()) {
			if (MathUtils.random() < 0.1) {
				response.getActions()
						.add(ActionSystem.Action.SPRINT);
			}
		}
		if (context.getEnvironment() == AreaComponent.AreaType.TOWER && MathUtils.random() < 0.1) {
			response.getActions()
					.add(ActionSystem.Action.TOWER_LEAVE);
		}
		response.getActions()
				.add(ActionSystem.Action.TOWER_ENTER);
		response.getActions()
				.add(ActionSystem.Action.WINDOW_DESTROY);
		if (MathUtils.random() < 0.5) {
			response.getActions()
					.add(ActionSystem.Action.DOOR_OPEN);
		} else {
			response.getActions()
					.add(ActionSystem.Action.DOOR_OPEN_SILENT);
		}
		if (MathUtils.random() < 0.001) {
			response.getActions()
					.add(ActionSystem.Action.MARKER_PLACE);
			response.setMarkerNumber(0);
			response.setDecayRate(0.5f);
		}
		return response;
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

