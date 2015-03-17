package com.the.machine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.BiMap;
import com.the.machine.components.AgentSightComponent;
import com.the.machine.components.WorldMapComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
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

        agentSightComponent.areaMapping.clear();
        Vector2 agentPosition = agentTransformComponent.get2DPosition();
        float agentAngle = -agentTransformComponent.getZRotation();

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
                    if (!agentSightComponent.areasBeingSeen.contains(areaPosition)) {
                        Entity white = new Entity();
                        white.add(AgentSightComponent.whiteSprite);
                        white.add(new TransformComponent().set2DPosition(areaPosition).setScale(0.02f));
                        getWorld().addEntity(white);
                        agentSightComponent.areasBeingSeen.add(areaPosition);  // Makes the simulation much faster
                    }
                }
            }
        }


        // Only display debug info every 2 seconds
        if (agentSightComponent.timeSinceLastDebugOutput >= 2) {
            agentSightComponent.timeSinceLastDebugOutput = 0;
            System.out.println(entity.getComponent(NameComponent.class).getName() + "'s sight component contains " + agentSightComponent.areaMapping.size() + " coordinates");
        }
        else {
            agentSightComponent.timeSinceLastDebugOutput += deltaTime;
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
