package com.the.machine.behaviours;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.*;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.systems.ActionSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frans on 25-4-2015.
 */

@NoArgsConstructor
public class AntColonyBehaviour implements BehaviourComponent.Behaviour<AntColonyBehaviour.AntColonyBehaviourState> {
    public enum AgentType { GUARD, INTRUDER };
    public final float timeBetweenPheromones = 2.5f;

    @Override
    public BehaviourComponent.BehaviourResponse<AntColonyBehaviourState> evaluate(BehaviourComponent.BehaviourContext context, AntColonyBehaviourState state) {
        float delta = context.getPastTime();
        state.nextSpeedChange -= delta;
        state.nextTurnChange -= delta;
        state.nextMarkerDrop -= delta;
        BehaviourComponent.BehaviourResponse<AntColonyBehaviourState> response = new BehaviourComponent.BehaviourResponse<>(context.getCurrentMovementSpeed(), context.getCurrentTurningSpeed(), new ArrayList<>(), state, 0, 0);
        AgentComponent agentComponent = state.agent.getComponent(AgentComponent.class); // The agentComponent is required to determine the movement speed

        // Update nextSpeedChange
        if (state.nextSpeedChange <= 0) {
            response.setMovementSpeed(agentComponent.getBaseMovementSpeed());
            state.nextSpeedChange = nextTime(0.5f)*10;
        }

        // Increase the speed if there are a lot of pheromones

        // Update nextTurnChange
        if (state.nextTurnChange <= 0) {
            state.nextTurnChange = nextTime(1f);
            TransformComponent transformComponent = state.agent.getComponent(TransformComponent.class);
            Vector2 averageLocationOfVisibleMarkers = getAverageLocationOfVisibleMarkers(context.getMarkers());

//            // If the guard can see an intruder, move towards it
//            if (state.agentType == AgentType.GUARD) {
//                if ()
//            }
            // If the agent can see markers, there is a chance that he will go to the avg location
            if (!canSeeMarkers(context) || Math.random() <= 0.5) {
                // Search for other entities (by looking around) while walking normally

                // Set the turnspeed to a low value to have a broader view
                response.setTurningSpeed((float) (50 * Math.random() - 25));
            }
            else {
                float averageAngleOfVisibleMarkers = new Vector2(averageLocationOfVisibleMarkers).sub(transformComponent.get2DPosition()).angle();
                float currentAngle = transformComponent.getZRotation();
                response.setTurningSpeed(turnSpeedForRotatingFromTo(currentAngle, averageAngleOfVisibleMarkers, 1f));
            }
        }

        // Update nextMarkerDrop
        if (state.nextMarkerDrop <= 0) {
            addMarker(getMarkerNumberForAgentType(state.agentType), 0.2f, response);
            state.nextMarkerDrop += timeBetweenPheromones;
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
        float nextMarkerDrop;
        Entity agent;           // Can probably be accessed in another way
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
     * Let the agent place a marker. The marker can be given a number, and a decay rate.
     * @param marketNumber
     * @param decayRate
     * @param response
     */
    private void addMarker(int marketNumber, float decayRate, BehaviourComponent.BehaviourResponse<AntColonyBehaviourState> response) {
        addAction(ActionSystem.Action.MARKER_PLACE, response);
        response.setMarkerNumber(marketNumber);
        response.setDecayRate(decayRate);
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

    /**
     * Returns true true if the agent can see any markers
     * @param context
     * @return
     */
    private boolean canSeeMarkers(BehaviourComponent.BehaviourContext context) {
        return context.getMarkers().size() != 0;
    }

    /**
     * Calculates the average location (the center point) of the group of markers in the context.
     *
     * @param markerList the list (derived from the context) in which the markers are stored
     * @return
     */
    private Vector2 getAverageLocationOfVisibleMarkers(List<WeakReference<Entity>> markerList) {
        Vector2 locationSummed = new Vector2();

        for (int i = 0; i < markerList.size(); i++) {
            Entity markerComponent = markerList.get(i).get();
            TransformComponent otherTransform = markerComponent.getComponent(TransformComponent.class);

            locationSummed.add(otherTransform.get2DPosition());
        }
        return locationSummed.scl((float) 1.0 / markerList.size());
    }

    /**
     * Calculates the required rotation speed to end at the goal angle. (A rough estimation would be enough, as the
     * required rotation speed is calculated again in the next frame)
     *
     * @param currentAngle the current angle of the agent
     * @param goalAngle the goal towards which the agent must be rotated
     * @param timeToRotate the time we have for rotating
     * @return
     */
    private float turnSpeedForRotatingFromTo(float currentAngle, float goalAngle, float timeToRotate) {
        float angleDifference = goalAngle - currentAngle;
        float rotationSpeed = angleDifference / timeToRotate;

        return rotationSpeed;
    }
}
