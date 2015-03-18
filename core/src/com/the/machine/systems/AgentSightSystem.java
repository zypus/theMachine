package com.the.machine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AgentSightComponent;
import com.the.machine.components.WorldMapComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.DelayedRemovalComponent;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.ShapeRenderComponent;
import com.the.machine.framework.components.TransformComponent;

import java.util.Map;

/**
 * Created by Frans on 15-3-2015.
 */
public class AgentSightSystem extends IteratingSystem {
    public AgentSightSystem(int priority) {
        super(Family.all(
                AgentSightComponent.class,  // To store the sight information
                TransformComponent.class    // The agent needs a location and a direction to be able to see things
        ).get(), priority);
    }

    @Override
    /*
     * Update the SightComponent for this Entity.
     */
    protected void processEntity(Entity entity, float deltaTime) {
        AgentSightComponent agentSightComponent = entity.getComponent(AgentSightComponent.class);
        TransformComponent agentTransformComponent = entity.getComponent(TransformComponent.class);

        // Update the debug info every second
        boolean isTimeToShowDebugInfo = false;
        if (agentSightComponent.timeSinceLastDebugOutput >= 0.1) {
            agentSightComponent.timeSinceLastDebugOutput = 0;
            isTimeToShowDebugInfo = true;
        }
        agentSightComponent.timeSinceLastDebugOutput += deltaTime;

        agentSightComponent.areaMapping.clear();
        Vector2 agentPosition = agentTransformComponent.get2DPosition();
        float agentAngle = -agentTransformComponent.getZRotation(); // ...

        Map<Vector2, Entity> worldMap = WorldMapComponent.worldMap;
        float maximumSightDistance = agentSightComponent.maximumSightDistance;

        for (Vector2 areaPosition : worldMap.keySet()) {
            if (agentPosition.dst(areaPosition) <= maximumSightDistance ) {
                float minAngle = normalizeAngle(agentAngle - (float) (0.5 * agentSightComponent.degreesOfSight));
                float maxAngle = normalizeAngle(agentAngle + (float) (0.5 * agentSightComponent.degreesOfSight));
                float angleOfArea = (new Vector2(areaPosition).sub(agentPosition)).angle();
                if (isAngleBetween(minAngle, angleOfArea, maxAngle)) {
                    agentSightComponent.areaMapping.put(areaPosition, worldMap.get(areaPosition));

                    // For debugging, add a small white dot if an agent has seen the area
                    // For debugging. Add a white pixel to areas that are on the map
                    if (isTimeToShowDebugInfo) {
                        Entity circle = new Entity();
                        circle.add(new ShapeRenderComponent().add((r) -> r.circle(0, 0, 2f)));
                        circle.add(new TransformComponent().set2DPosition(areaPosition).setScale(.01f).setZ(1));
                        circle.add(new DelayedRemovalComponent().setDelay(0.4f));
                        getWorld().addEntity(circle);
                    }
                }
            }
        }
    }

    private static boolean isAngleBetween(float lowestAngle, float angleToMeasure, float highestAngle) {
        if (highestAngle <= lowestAngle) {
            return lowestAngle <= angleToMeasure || angleToMeasure <= highestAngle;
        }
        else {
            return lowestAngle <= angleToMeasure && angleToMeasure <= highestAngle;
        }
    }

    private static float normalizeAngle(float angle) {
        // Make sure that angle is between 0 and 360
        return ((angle % 360) + 360) % 360;
    }
}
