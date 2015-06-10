package com.the.machine.behaviours;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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
    TODO 1 refactor code. Looks messy.
    TODO 2 broken windows are also considered to be structures. Use another test to determine whether an area causes a collision
    TODO 3 differentiate between marker types (One for moving towards it, one for moving away)
    TODO 4 for guards, making moving towards an intruder has a higher priority than moving towards another guard
    TODO 5 make Marker vanished error not happen
    TODO 6 remember map (especially positions of other agents)
    TODO 7 algorithm is really bad for guards at finding intruders. May have something to do with #6
    TODO 8 make time between pheromones linear instead of exponential if possible
     */
    public enum AgentType { GUARD, INTRUDER };

    @Override
    public List<BehaviourComponent.BehaviourResponse> evaluate(BehaviourComponent.BehaviourContext context, AntColonyBehaviourState state) {
        updateCountdowns(state, context.getPastTime());     // Update the timers for rotating, dropping markers, etc.

        List<BehaviourComponent.BehaviourResponse> responses = new ArrayList<>();

        // Initialize speed
        if (context.getCurrentMovementSpeed() == 0) {
            initializeSpeed(state, responses);
        }

        lookAround(state, context);

        // Update nextRotationUpdate
        if (state.nextRotationUpdate <= 0) {
            state.nextRotationUpdate = state.rotationResetTime;
            forgetWhatAgentHasSeen(state);

            TransformComponent transformComponent = state.agent.getComponent(TransformComponent.class);
            boolean agentIsAlreadyUpdatingRotation = false;


            if (state.agentType == AgentType.GUARD) {
                // The guard only responds to intruders for now TODO
                if (state.nearestIntruderSeen != null) {
                    Vector2 relativePosOfNearestAgent = relativePositionOf(state, state.nearestIntruderSeen);
                    rotateTowards(responses, relativePosOfNearestAgent);
                    agentIsAlreadyUpdatingRotation = true;
                }
            }
            else { // if state.agentType == AgentType.Intruder
                // Move to the opposite direction
                // Intruders want to move away from guards, and if they also move away from other intruders they will
                // cover a bigger area
                Vector2 nearestTargetArea = getNearestAreaOfType(AreaComponent.AreaType.TARGET, state, context);
                if (nearestTargetArea != null) {
                    System.out.println("Moving towards target area");
                    Vector2 relativePosOfNearestArea = new Vector2(nearestTargetArea).sub(transformComponent.get2DPosition());
                    rotateTowards(responses, relativePosOfNearestArea);
                    agentIsAlreadyUpdatingRotation = true;
                }
                else { // if there is no target area found
                    if (state.nearestGuardSeen != null) {
                        System.out.println(state.agent.getComponent(NameComponent.class).getName() + " is moving away from other agent at " + state.nearestGuardSeen);
                        // Turn to the opposite direction
                        Vector2 negRelativePositionOfNearestAgent = relativePositionOf(state, state.nearestGuardSeen).scl(-1);
                        rotateTowards(responses, negRelativePositionOfNearestAgent);
                        agentIsAlreadyUpdatingRotation = true;
                    }
                }
            }

            if (!agentIsAlreadyUpdatingRotation) {
                // If the agent can see markers, there is a chance that he will go to the avg location
                Vector2 avgMarkerPosition = getAverageLocationOfVisibleMarkers(context.getMarkers());
                if (avgMarkerPosition == null || Math.random() <= 0.5) {
                    // Search for other entities (by turning a bit) while walking normally
                    Vector2 currentDirection = context.getMoveDirection();
                    Vector2 relativePositionOfCollision = relativePositionOf(state, state.edgeOfSomethingPosition);
                    Vector2 randomDirection = new Vector2(currentDirection).rotate((float) (50 * Math.random() - 25));

                    // If we have found a place where a collision will happen, move opposite to it
                    Vector2 newDirection = relativePositionOfCollision == null ? randomDirection : new Vector2(relativePositionOfCollision).scl(-1);
                    rotateTowards(responses, newDirection);
                }
                else {
                    responses.add(new BehaviourComponent.BehaviourResponse(
                            ActionSystem.Action.TURN,
                            new ActionSystem.TurnData(avgMarkerPosition, 30f)
                    ));
                }
            }
        }

        // Update nextMarkerdropUpdate
        if (state.nextMarkerdropUpdate <= 0) {
            state.nextMarkerdropUpdate += state.markerDropResetTime;
            addMarker(0, 0.2f, responses);  // Add a marker of type 0
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
        final AgentType agentType;
        final Entity agent;
        Vector2 edgeOfSomethingPosition;    // The edge of the world that the entity has collided with. Tries to move away from it
        final float agentSpeed;

        // The positions of objects that the entity remembers
        Vector2 nearestGuardSeen;
        Vector2 nearestIntruderSeen;
        Vector2 nearestTargetAreaSeen;
        List<Vector2> markerPositionsSeen;

        // The time until the next update of speed, rotation, etc.
        float nextRotationUpdate;
        float nextMarkerdropUpdate;

        // The amount to which the timers are reset when they have reached 0
        final float markerDropResetTime;
        final float rotationResetTime;
    }

    /**
     * Used for external methods when initializing an AntColonyBehaviour. Contains a set of tested parameters.
     * @param agentType
     * @param agent
     * @return
     */
    public static AntColonyBehaviourState getInitialState(AgentType agentType, Entity agent) {
        return new AntColonyBehaviour.AntColonyBehaviourState(
                agentType, agent, null, 5,  // agentType, agent, edgeOfSomethingPosition, agentSpeed
                null, null, null, null,     // Positions of objects that the agent remembered
                0, 0,                       // How much time it takes until the next rotation and marker dropping
                2f, 0.5f                    // Value to which the timer is reset
        );
    }

    private void initializeSpeed(AntColonyBehaviourState state, List<BehaviourComponent.BehaviourResponse> responses) {
        responses.add(new BehaviourComponent.BehaviourResponse(
                ActionSystem.Action.MOVE,
                new ActionSystem.MoveData(state.agentSpeed)
        ));
    }
    private void updateCountdowns(AntColonyBehaviourState state, float delta) {
        state.nextRotationUpdate -= delta;
        state.nextMarkerdropUpdate -= delta;
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
        Vector2 locationSummed = new Vector2();
        boolean markerFound = false;

        for (WeakReference<Entity> aMarkerList : markerList) {
            markerFound = true;
            Entity markerComponent = aMarkerList.get();
            TransformComponent otherTransform = null;
            if (markerComponent != null) {
                otherTransform = markerComponent.getComponent(TransformComponent.class);
            }
            else {
                // If markerComponent == null
                // Old markers can still be found, but they don't have a markerComponent
                return null;
            }

            locationSummed.add(otherTransform.get2DPosition());
        }

        if (markerFound) {
            return locationSummed.scl((float) 1.0 / markerList.size());
        }
        return null;
    }

    private Vector2 getNearestAreaOfType(AreaComponent.AreaType areaType, AntColonyBehaviourState state, BehaviourComponent.BehaviourContext context) {
        List<DiscreteMapComponent.MapCell> visibleAreas = context.getVision();
        DiscreteMapComponent.MapCell nearestArea = null;
        float distanceToNearestArea = Float.MAX_VALUE;
        TransformComponent transformComponent = state.agent.getComponent(TransformComponent.class);

        for (DiscreteMapComponent.MapCell visibleArea : visibleAreas ) {
            if (visibleArea.getType() == areaType) {
                float distanceToArea = visibleArea.getPosition().dst2(transformComponent.get2DPosition());
                if (distanceToArea == Math.min(distanceToNearestArea, distanceToArea)) {
                    distanceToNearestArea = distanceToArea;
                    nearestArea = visibleArea;
                    System.out.println("Found area of type " + areaType);
                }
            }
        }

        return nearestArea == null ? null : nearestArea.getPosition();
    }


    /**
     * Looks around. Stores all objects seen since the last update in a list
     * @param state
     * @param context
     */
    private void lookAround(AntColonyBehaviourState state, BehaviourComponent.BehaviourContext context) {
        /*
        TODO: update the values of:
          - Vector2 nearestTargetAreaSeen;
          - List<Vector2> markerPositionsSeen;
         */

        // Update the positions of agents
        float nearestGuardDistance = state.nearestGuardSeen == null ? Float.MAX_VALUE : distanceBetweenAgentAndOther(state, state.nearestGuardSeen);
        float nearestIntruderDistance = state.nearestIntruderSeen == null ? Float.MAX_VALUE : distanceBetweenAgentAndOther(state, state.nearestIntruderSeen);
        for (WeakReference<Entity> agentReference : context.getAgents()) {
            Entity otherAgent = agentReference.get();

            String otherAgentName = otherAgent.getComponent(NameComponent.class).getName();
            TransformComponent otherAgentTransform = otherAgent.getComponent(TransformComponent.class);
            Vector2 otherAgentPosition = otherAgentTransform.get2DPosition();
            float newDistance = distanceBetweenAgentAndOther(state, otherAgentPosition);

            if (otherAgentName.equals("Intruder")) {
                if (Math.min(newDistance, nearestIntruderDistance) == newDistance) {
                    nearestIntruderDistance = newDistance;
                    state.nearestIntruderSeen = otherAgentPosition;
                }
            }
            else if (otherAgentName.equals("Agent")) {  // For some reason Guards are just called 'Agent'
                if (Math.min(newDistance, nearestGuardDistance) == newDistance) {
                    nearestGuardDistance = newDistance;
                    state.nearestGuardSeen = otherAgentPosition;
                }
            }
            else {
                System.err.println("Agent with name " + otherAgentName + " was not recognized");
            }
        }

        // Update the position of the collisions
        // Determine where the nearest potential collision will be
        float nearestCollisionDistance = state.edgeOfSomethingPosition == null ? Float.MAX_VALUE : distanceBetweenAgentAndOther(state, state.edgeOfSomethingPosition);
        for (DiscreteMapComponent.MapCell cell : context.getVision()) {
            float distanceToCell = distanceBetweenAgentAndOther(state, cell.getPosition());
            if (cell.getType().isStructure()) {
                if (Math.min(distanceToCell, nearestCollisionDistance) == distanceToCell) {
                    nearestCollisionDistance = distanceToCell;
                    state.edgeOfSomethingPosition = cell.getPosition();
                }
            }
        }
    }

    private static float distanceBetweenAgentAndOther(AntColonyBehaviourState state, Vector2 other) {
        return relativePositionOf(state, other).len();
    }

    private static Vector2 relativePositionOf(AntColonyBehaviourState state, Vector2 other) {
        try {
            TransformComponent transform = state.agent.getComponent(TransformComponent.class);
            return new Vector2(other).sub(transform.get2DPosition());
        } catch(Exception e) { return null; }
    }

    private void rotateTowards(List<BehaviourComponent.BehaviourResponse> responses, Vector2 newRelativePosition) {
        responses.add(new BehaviourComponent.BehaviourResponse(
                ActionSystem.Action.TURN,
                new ActionSystem.TurnData(newRelativePosition, 30f)));
    }

    private void forgetWhatAgentHasSeen(AntColonyBehaviourState state) {
        state.nearestGuardSeen = null;
        state.nearestIntruderSeen = null;
        state.nearestTargetAreaSeen = null;
        state.markerPositionsSeen = null;
    }
}
