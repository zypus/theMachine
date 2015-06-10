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
    TODO 5 make Marker vanished error not happen
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

        lookAround(state, context); // Update the locations of everything the agent has seen

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
                if (state.nearestTargetAreaSeen != null) {
                    System.out.println("Moving towards target area");
                    Vector2 relativePosOfNearestArea = relativePositionOf(state, state.nearestTargetAreaSeen);
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
                Vector2 avgMarkerPosition = getAverageLocationOfMarkersSeen(state.markerPositionsSeen);
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
            if (MathUtils.random() <= 0.1) {
                responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.SPRINT, null));
            }
        }
        if (context.getEnvironment() == AreaComponent.AreaType.TOWER) {
            if (MathUtils.random() <= 0.1) {
                responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.TOWER_LEAVE, null));
            }
        }

        // Always try to enter a tower (even if there is no tower nearby)
        responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.TOWER_ENTER, null));

        // Also, always try to destroy a window (even if there are none nearby)
        responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.WINDOW_DESTROY, null));

        // A 50% chance of opening a door normally. If the door isn't opened normally,
        // it will be opened silently.
        if (Math.random() <= 0.5) {
            responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.DOOR_OPEN, null));
        }
        else {
            responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.DOOR_OPEN_SILENT, null));
        }

        return responses;
    }

    @AllArgsConstructor
    public static class AntColonyBehaviourState implements BehaviourComponent.BehaviourState {
        final AgentType agentType;
        final Entity agent;
        final float agentSpeed;

        // The positions of objects that the entity remembers
        Vector2 nearestGuardSeen;
        Vector2 nearestIntruderSeen;
        Vector2 nearestTargetAreaSeen;
        List<Vector2> markerPositionsSeen;
        Vector2 edgeOfSomethingPosition;    // The edge of the world that the entity has collided with. Tries to move away from it


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
                agentType, agent, 5,            // agentType, agent, agentSpeed
                null, null, null, null, null,   // Positions of objects that the agent remembered
                0, 0,                           // How much time it takes until the next rotation and marker dropping
                2f, 0.5f                        // Value to which the timer is reset
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
     * Let the agent place a marker. The marker can be given a number, and a decay rate. The marker placement will be added
     * to the responses list
     *
     * @param marketNumber
     * @param decayRate
     * @param responses
     */
    private void addMarker(int marketNumber, float decayRate, List<BehaviourComponent.BehaviourResponse> responses) {
        responses.add(new BehaviourComponent.BehaviourResponse(
                ActionSystem.Action.MARKER_PLACE,
                new ActionSystem.MarkerData(marketNumber, decayRate)
        ));
    }

    private Vector2 getAverageLocationOfMarkersSeen(List<Vector2> markerPositionsList) {
        Vector2 locationSummed = new Vector2();
        boolean markerFound = false;

        for (Vector2 markerPosition : markerPositionsList) {
            markerFound = true;
            locationSummed.add(markerPosition);
        }

        if (markerFound) {
            return locationSummed.scl((float) 1.0 / markerPositionsList.size());
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

        // Update the position of the areas
        // Determine where the nearest potential collision will be
        float nearestCollisionDistance = state.edgeOfSomethingPosition == null ? Float.MAX_VALUE : distanceBetweenAgentAndOther(state, state.edgeOfSomethingPosition);
        float nearestTargetAreaDistance = state.nearestTargetAreaSeen == null ? Float.MAX_VALUE : distanceBetweenAgentAndOther(state, state.nearestTargetAreaSeen);

        for (DiscreteMapComponent.MapCell cell : context.getVision()) {
            float distanceToCell = distanceBetweenAgentAndOther(state, cell.getPosition());

            // Collidables // TODO create own check whether a structure is collidable
            if (cell.getType().isStructure()) {
                if (Math.min(distanceToCell, nearestCollisionDistance) == distanceToCell) {
                    nearestCollisionDistance = distanceToCell;
                    state.edgeOfSomethingPosition = cell.getPosition();
                }
            }

            // Target area
            if (cell.getType() == AreaComponent.AreaType.TARGET) {
                if (Math.min(distanceToCell, nearestTargetAreaDistance) == distanceToCell) {
                    nearestTargetAreaDistance = distanceToCell;
                    state.nearestTargetAreaSeen = cell.getPosition();
                }
            }
        }

        // Update the marker positions
        for (WeakReference<Entity> markerReference : context.getMarkers()) {
            Entity marker = markerReference.get();
            TransformComponent transformOfMarker = marker.getComponent(TransformComponent.class);

            // If the marker still exists
            if (transformOfMarker != null) {
                Vector2 markerPosition = marker.getComponent(TransformComponent.class).get2DPosition();
                if (!state.markerPositionsSeen.contains(markerPosition)) {
                    state.markerPositionsSeen.add(markerPosition);
                }
            }
        }
    }

    private static float distanceBetweenAgentAndOther(AntColonyBehaviourState state, Vector2 other) {
        return other == null ? Float.MAX_VALUE : relativePositionOf(state, other).len();
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
        state.markerPositionsSeen = new ArrayList<Vector2>();
    }
}
