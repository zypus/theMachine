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
    /*
    TODO if a collision has been detected, move away from it
    TODO differentiate between marker types (One for moving towards it, one for moving away)
    TODO for guards, making moving towards an intruder has a higher priority than moving towards another guard
    TODO remember map
     */
    public enum AgentType { GUARD, INTRUDER };
    public final float timeBetweenPheromones = 2.5f;

    @Override
    public List<BehaviourComponent.BehaviourResponse> evaluate(BehaviourComponent.BehaviourContext context, AntColonyBehaviourState state) {
        float delta = context.getPastTime();
        state.nextSpeedChange -= delta;
        state.nextTurnChange -= delta;
        state.nextMarkerDrop -= delta;

        List<BehaviourComponent.BehaviourResponse> responses = new ArrayList<>();

        // Update nextSpeedChange
        if (state.nextSpeedChange <= 0) {
            responses.add(new BehaviourComponent.BehaviourResponse(
                            ActionSystem.Action.MOVE,
                            new ActionSystem.MoveData(5)
                    )
            );
            state.nextSpeedChange = 2;
        }

        // Update nextTurnChange
        if (state.nextTurnChange <= 0) {
            state.nextTurnChange = 0.5f;
            TransformComponent transformComponent = state.agent.getComponent(TransformComponent.class);
            boolean agentIsAlreadyUpdatingPosition = false;


            // Determine where the nearest potential collision will be
            float nearestStructureDistance = Float.MAX_VALUE;
            Vector2 relativePositionOfCollision = null;
            for (DiscreteMapComponent.MapCell cell : context.getVision()) {
                if (cell.getType().isStructure()) {
                    Vector2 relativePositonOfCell = new Vector2(cell.getPosition()).sub(transformComponent.get2DPosition());
                    if (nearestStructureDistance != Math.min(relativePositonOfCell.len(), nearestStructureDistance)) {
                        nearestStructureDistance = relativePositonOfCell.len();
                        relativePositionOfCollision = relativePositonOfCell;
                    }
                }
            }

            // Guards should move to the nearest agent // TODO a near intruder has a higher priority
            if (state.agentType == AgentType.GUARD) {
                Vector2 nearestAgentVector = getNearestAgentTransform(state, context);
                if (nearestAgentVector != null) {
                    Vector2 relativePosOfNearestAgent = new Vector2(nearestAgentVector).sub(transformComponent.get2DPosition());
                    responses.add(new BehaviourComponent.BehaviourResponse(
                            ActionSystem.Action.TURN,
                            new ActionSystem.TurnData(relativePosOfNearestAgent, 30f)));
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
                    Vector2 relativePosOfNearestArea = new Vector2(nearestTargetArea).sub(transformComponent.get2DPosition());
                    responses.add(new BehaviourComponent.BehaviourResponse(
                            ActionSystem.Action.TURN,
                            new ActionSystem.TurnData(relativePosOfNearestArea, 30f)
                    ));
                    agentIsAlreadyUpdatingPosition = true;
                }
                else { // if there is no target area found
                    Vector2 nearestAgentVector = getNearestAgentTransform(state, context);
                    if (nearestAgentVector != null) {
                        System.out.println(state.agent.getComponent(NameComponent.class).getName() + " is moving away from other agent at " + nearestAgentVector);
                        // Turn to the opposite direction
                        Vector2 negRelativePositionOfNearestAgent = new Vector2(nearestAgentVector).sub(transformComponent.get2DPosition());
                        responses.add(new BehaviourComponent.BehaviourResponse(
                                ActionSystem.Action.TURN,
                                new ActionSystem.TurnData(negRelativePositionOfNearestAgent, 30f)
                        ));
                        agentIsAlreadyUpdatingPosition = true;
                    }
                }
            }

            if (!agentIsAlreadyUpdatingPosition) {
                // If the agent can see markers, there is a chance that he will go to the avg location
                Vector2 avgMarkerPosition = getAverageLocationOfVisibleMarkers(context.getMarkers());
                if (avgMarkerPosition == null || Math.random() <= 0.5) {
                    // Search for other entities (by turning a bit) while walking normally
                    Vector2 currentDirection = context.getMoveDirection();

                    Vector2 randomDirection = new Vector2(currentDirection).rotate((float) (50 * Math.random() - 25));

                    // If we have found a place where a collision will happen, move opposite to it
                    Vector2 newDirection = relativePositionOfCollision == null ? randomDirection : new Vector2(relativePositionOfCollision).scl(-1);
                    responses.add(new BehaviourComponent.BehaviourResponse(
                            ActionSystem.Action.TURN,
                            new ActionSystem.TurnData(newDirection, 30f)
                    ));
                }
                else {
                    responses.add(new BehaviourComponent.BehaviourResponse(
                            ActionSystem.Action.TURN,
                            new ActionSystem.TurnData(avgMarkerPosition, 30f)
                    ));
                }
            }
        }

        // Update nextMarkerDrop
        if (state.nextMarkerDrop <= 0) {
            addMarker(0, 0.2f, responses);  // Add a marker of type 0
            state.nextMarkerDrop += timeBetweenPheromones;
        }

        if (context.isCanSprint()) {
            addActionWithProbability(ActionSystem.Action.SPRINT, 0.1, responses);
        }
        if (context.getEnvironment() == AreaComponent.AreaType.TOWER) {
            addActionWithProbability(ActionSystem.Action.TOWER_LEAVE, 0.1, responses);
        }

        // Always try to enter a tower (even if there is no tower nearby)
        addResponse(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.TOWER_ENTER, null), responses);

        // Also, always try to destroy a window (even if there are none nearby)
        addResponse(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.WINDOW_DESTROY, null), responses);

        // A 50% chance of opening a door normally. If the door isn't opened normally,
        // it will be opened silently.
        if (Math.random() <= 0.5) {
            addResponse(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.DOOR_OPEN, null), responses);
        }
        else {
            addResponse(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.DOOR_OPEN_SILENT, null), responses);
        }

        return responses;
    }

    @AllArgsConstructor
    public static class AntColonyBehaviourState implements BehaviourComponent.BehaviourState {
        float nextSpeedChange;
        float nextTurnChange;
        AgentType agentType;
        float nextMarkerDrop;
        Entity agent;
        Vector2 edgeOfSomethingPosition;    // The edge of the world that the entity has collided with. Tries to move away from it
    }

    public static AntColonyBehaviourState getInitialState(AgentType agentType, Entity agent) {
        return new AntColonyBehaviour.AntColonyBehaviourState(0, 0, agentType, 2, agent, null);
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
        for (Vector2 noiseLocation : context.getSoundDirections()) {
            float distanceWithThisNoise = noiseLocation.dst2(transformComponent.get2DPosition());
            if (distanceWithThisNoise == Math.min(distanceWithNearestAgent, distanceWithThisNoise)) {
                distanceWithNearestAgent = distanceWithThisNoise;
                nearestNoisePosition = noiseLocation;
            }
        }

        return nearestNoisePosition;
    }

    /**
     * Add a given action with a given probability. Only if the response requires no additional data
     * @param action the action to be added to the behaviour
     * @param probability the probability that the action will be added
     * @param responses the responses list to which the action will be added
     */
    private void addActionWithProbability(ActionSystem.Action action,
                                          double probability,
                                          List<BehaviourComponent.BehaviourResponse> responses) {
        if (MathUtils.random() < probability) {
            addResponse(new BehaviourComponent.BehaviourResponse(action, null), responses);
        }
    }

    /**
     * Add a given action to the response
     * @param response response to be added
     * @param responses the response to which the action should be added
     */
    private void addResponse(BehaviourComponent.BehaviourResponse response, List<BehaviourComponent.BehaviourResponse> responses) {
        responses.add(response);
    }

    /**
     * Let the agent place a marker. The marker can be given a number, and a decay rate. The marker placement will be added
     * to the responses list
     *
     * @param marketNumber
     * @param decayRate
     * @param responses
     */
    private void addMarker(int marketNumber, float decayRate, List<BehaviourComponent.BehaviourResponse> responses) {
        addResponse(new BehaviourComponent.BehaviourResponse(
                ActionSystem.Action.MARKER_PLACE,
                new ActionSystem.MarkerData(marketNumber, decayRate)
        ), responses);
    }

    /**
     * Calculates the average location (the center point) of the group of markers in the context.
     *
     * @param markerList the list (derived from the context) in which the markers are stored
     * @return
     */
    private Vector2 getAverageLocationOfVisibleMarkers(List<WeakReference<Entity>> markerList) {
        try {
            Vector2 locationSummed = new Vector2();
            boolean markerFound = false;

            for (int i = 0; i < markerList.size(); i++) {
                markerFound = true;
                Entity markerComponent = markerList.get(i).get();
                TransformComponent otherTransform = markerComponent.getComponent(TransformComponent.class);

                locationSummed.add(otherTransform.get2DPosition());
            }

            if (markerFound) {
                return locationSummed.scl((float) 1.0 / markerList.size());
            }
        } catch (NullPointerException e) {
            System.err.println("Marker vanished");
        }
        finally {
            return null;
        }
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
