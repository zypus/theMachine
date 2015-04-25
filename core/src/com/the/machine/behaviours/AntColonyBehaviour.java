package com.the.machine.behaviours;

import com.badlogic.gdx.math.MathUtils;
import com.the.machine.components.AgentSightComponent;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.systems.ActionSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

/**
 * Created by Frans on 25-4-2015.
 */

@NoArgsConstructor
public class AntColonyBehaviour implements BehaviourComponent.Behaviour<AntColonyBehaviour.AntColonyBehaviourState> {
    public enum AgentType { GUARD, INTRUDER };

    @Override
    public BehaviourComponent.BehaviourResponse<AntColonyBehaviourState> evaluate(BehaviourComponent.BehaviourContext context, AntColonyBehaviourState state) {
        float delta = context.getPastTime();
        state.nextSpeedChange -= delta;
        state.nextTurnChange -= delta;
        BehaviourComponent.BehaviourResponse<AntColonyBehaviourState> response = new BehaviourComponent.BehaviourResponse<>(context.getCurrentMovementSpeed(), context.getCurrentTurningSpeed(), new ArrayList<>(), state, 0, 0);
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
            addActionWithProbability(ActionSystem.Action.SPRINT, 0.1, response);
        }
        if (context.getEnvironment() == AreaComponent.AreaType.TOWER) {
            addActionWithProbability(ActionSystem.Action.TOWER_LEAVE, 0.1, response);
        }

        // Always try to enter a tower (even if there is no tower nearby)
        if (!response.getActions().contains(ActionSystem.Action.TOWER_LEAVE)) {
            addAction(ActionSystem.Action.TOWER_ENTER, response);
        }

        // Also, always try to destroy a window (even if there are none nearby)
        addAction(ActionSystem.Action.WINDOW_DESTROY, response);

        // A 50% chance of opening a door normally. If the door isn't opened normally,
        // it will be opened silently.
        addActionWithProbability(ActionSystem.Action.DOOR_OPEN, 0.5, response);
        if (!response.getActions().contains(ActionSystem.Action.DOOR_OPEN)) {
            addAction(ActionSystem.Action.DOOR_OPEN_SILENT, response);
        }

        addMarkerWithProbability(0.001, getMarkerNumberForAgentType(state.agentType), 0.5f, response);

        return response;
    }

    private float nextTime(float rate) {
        return (float) (-Math.log(1 - MathUtils.random()) / rate);
    }

    @AllArgsConstructor
    public static class AntColonyBehaviourState implements BehaviourComponent.BehaviourState {
        float nextSpeedChange;
        float nextTurnChange;
        AgentType agentType;
    }

    /**
     * Add a given action with a given probability.
     * @param action the action to be added to the behaviour
     * @param probability the probability that the action will be added
     * @param response the response to which the action will be added
     */
    private void addActionWithProbability(ActionSystem.Action action,
                                          double probability,
                                          BehaviourComponent.BehaviourResponse<AntColonyBehaviourState> response) {
        if (MathUtils.random() < probability) {
            addAction(action, response);
        }
    }

    /**
     * Add a given action to the response
     * @param action action to be added
     * @param response the response to which the action should be added
     */
    private void addAction(ActionSystem.Action action, BehaviourComponent.BehaviourResponse<AntColonyBehaviourState> response) {
        response.getActions().add(action);
    }

    /**
     * Let the agent place a marker with a given probability. The marker can be given a number, and a decay rate.
     * @param probability
     * @param marketNumber
     * @param decayRate
     * @param response
     */
    private void addMarkerWithProbability(double probability, int marketNumber, float decayRate, BehaviourComponent.BehaviourResponse<AntColonyBehaviourState> response) {
        if (MathUtils.random() < probability) {
            addAction(ActionSystem.Action.MARKER_PLACE, response);
            response.setMarkerNumber(marketNumber);
            response.setDecayRate(0.001f);
        }
    }

    private int getMarkerNumberForAgentType(AgentType agentType) {
        if (agentType == AgentType.GUARD) {
            return 0;
        }
        else if (agentType == AgentType.INTRUDER) {
            return 1;
        }
        else return 100;
    }
}
