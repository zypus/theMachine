package com.the.machine.behaviours;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.DiscreteMapComponent;
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
            state.nextSpeedChange = 2;
        }

        // Update nextTurnChange
        if (state.nextTurnChange <= 0) {
            state.nextTurnChange = 0.5f;
            TransformComponent transformComponent = state.agent.getComponent(TransformComponent.class);
            Vector2 averageLocationOfVisibleMarkers = getAverageLocationOfVisibleMarkers(context.getMarkers());
            boolean agentIsAlreadyUpdatingPosition = false;

            // Guards should move to the nearest agent // TODO a near intruder has a higher priority
            if (state.agentType == AgentType.GUARD) {
                Vector2 nearestAgentVector = getNearestAgentTransform(state, context);
                if (nearestAgentVector != null) {
                    float angleOfNearestAgent = new Vector2(nearestAgentVector).sub(transformComponent.get2DPosition()).angle();
                    float currentAngle = transformComponent.getZRotation();
                    response.setTurningSpeed(turnSpeedForRotatingFromTo(currentAngle, angleOfNearestAgent, 0.015f));
                    agentIsAlreadyUpdatingPosition = true;
                }
            }
            else { // if state.agentType == AgentType.Intruder
                // Move to the opposite direction
                // Intruders want to move away from guards, and if they also move away from other intruders they will
                // cover a bigger area
                Vector2 nearestTargetArea = getNearestTargetArea(state, context);
                if (nearestTargetArea != null) {
                    System.out.println("Moving towards targat area");
                    float angleOfTargetArea = new Vector2(nearestTargetArea).sub(transformComponent.get2DPosition()).angle();
                    float currentAngle = transformComponent.getZRotation();
                    response.setTurningSpeed(-turnSpeedForRotatingFromTo(currentAngle, angleOfTargetArea, 0.015f));
                    agentIsAlreadyUpdatingPosition = true;
                }
                else {
                    Vector2 nearestAgentVector = getNearestAgentTransform(state, context);
                    if (nearestAgentVector != null) {
                        System.out.println(state.agent.getComponent(NameComponent.class).getName() + " is moving away from other agent at " + nearestAgentVector);
                        // Turn to the opposite direction
                        float angleOppositeOfNearestAgent = new Vector2(nearestAgentVector).sub(transformComponent.get2DPosition()).angle();
                        float currentAngle = transformComponent.getZRotation();
                        response.setTurningSpeed(turnSpeedForRotatingFromTo(currentAngle, angleOppositeOfNearestAgent, 0.015f));
                        agentIsAlreadyUpdatingPosition = true;
                    }
                }
            }

            if (!agentIsAlreadyUpdatingPosition) {
                // If the agent can see markers, there is a chance that he will go to the avg location
                if (!canSeeMarkers(context) || Math.random() <= 0.5) {
                    // Search for other entities (by turning a bit) while walking normally

                    // Set the turnspeed to a low value to have a broader view
                    response.setTurningSpeed((float) (50 * Math.random() - 25));
                }
                else {
                    float averageAngleOfVisibleMarkers = new Vector2(averageLocationOfVisibleMarkers).sub(transformComponent.get2DPosition()).angle();
                    float currentAngle = transformComponent.getZRotation();
                    response.setTurningSpeed(turnSpeedForRotatingFromTo(currentAngle, averageAngleOfVisibleMarkers, 0.015f));
                }
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

    @AllArgsConstructor
    public static class AntColonyBehaviourState implements BehaviourComponent.BehaviourState {
        float nextSpeedChange;
        float nextTurnChange;
        AgentType agentType;
        float nextMarkerDrop;
        Entity agent;
    }

    /**
     * Returns the TransformComponent of the nearest agent
     * @param state
     * @param context
     * @return
     */
    private Vector2 getNearestAgentTransform(AntColonyBehaviourState state, BehaviourComponent.BehaviourContext context) {
        TransformComponent transformComponent = state.agent.getComponent(TransformComponent.class);
        List<WeakReference<Entity>> visibleAgents = context.getAgents();

        TransformComponent nearestAgentTransform = null;
        float distanceWithNearestAgent = Float.MAX_VALUE;   // Everything is nearer than this

        for (WeakReference<Entity> visibleAgentReference : visibleAgents) {
            Entity visibleAgent = visibleAgentReference.get();
            float distanceWithThisAgent = visibleAgent.getComponent(TransformComponent.class).get2DPosition().dst2(transformComponent.get2DPosition());
            // Don't run away from yourself
            if (distanceWithThisAgent == Math.min(distanceWithNearestAgent, distanceWithThisAgent) && distanceWithThisAgent >= 0.01) {
                distanceWithNearestAgent = distanceWithThisAgent;
                nearestAgentTransform = visibleAgent.getComponent(TransformComponent.class);
            }
        }

        if (nearestAgentTransform != null) {
            return nearestAgentTransform.get2DPosition();
        }

        Vector2 nearestNoisePosition = null;
        // If no agent can be seen, listen if there is a sound
        // TODO don't make intruders run away from their own sound
//        for (Vector2 noiseLocation : context.getSoundDirections()) {
//            float distanceWithThisNoise = noiseLocation.dst2(transformComponent.get2DPosition());
//            if (distanceWithThisNoise == Math.min(distanceWithNearestAgent, distanceWithThisNoise)) {
//                distanceWithNearestAgent = distanceWithThisNoise;
//                nearestNoisePosition = noiseLocation;
//            }
//        }

        return nearestNoisePosition;
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

    private boolean agentSeesTargetArea(BehaviourComponent.BehaviourContext context) {
        List<DiscreteMapComponent.MapCell> visibleAreas = context.getVision();
        for (DiscreteMapComponent.MapCell visibleArea : visibleAreas ) {
            if (visibleArea.getType() == AreaComponent.AreaType.TARGET) {
                return true;
            }
        }
        return false;
    }

    private Vector2 getNearestTargetArea(AntColonyBehaviourState state, BehaviourComponent.BehaviourContext context) {

        List<DiscreteMapComponent.MapCell> visibleAreas = context.getVision();
        DiscreteMapComponent.MapCell nearestArea = null;
        float distanceToNearestArea = Float.MAX_VALUE;
        TransformComponent transformComponent = state.agent.getComponent(TransformComponent.class);

        for (DiscreteMapComponent.MapCell visibleArea : visibleAreas ) {
            if (visibleArea.getType() == AreaComponent.AreaType.TARGET) {
                float distanceToArea = visibleArea.getPosition().dst2(transformComponent.get2DPosition());
                if (distanceToArea == Math.min(distanceToNearestArea, distanceToArea)) {
                    distanceToNearestArea = distanceToArea;
                    nearestArea = visibleArea;
                    System.out.println("Found target area");
                }
            }
        }

        return nearestArea == null ? null : nearestArea.getPosition();
    }
}
