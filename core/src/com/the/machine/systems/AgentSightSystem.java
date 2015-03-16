package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.BiMap;
import com.the.machine.components.AgentSightComponent;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.VelocityComponent;
import com.the.machine.components.WorldMapComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.NameComponent;
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
    protected void processEntity(Entity entity, float deltaTime) {
        AgentSightComponent agentSightComponent = entity.getComponent(AgentSightComponent.class);
        TransformComponent agentTransformComponent = entity.getComponent(TransformComponent.class);

        agentSightComponent.areaMapping.clear();
        Vector2 agentPosition = agentTransformComponent.get2DPosition();
        BiMap<Vector2, Entity> worldMap = WorldMapComponent.worldMap;
        float maximumSightDistance = agentSightComponent.maximumSightDistance;

        for (Vector2 areaPosition : worldMap.keySet()) {
            if (agentPosition.dst(areaPosition) <= maximumSightDistance ) {

                // TODO take viewing angle into account

                // Don't add the entity itself to its SightComponent
                if (!worldMap.get(areaPosition).equals(entity)) {
                    agentSightComponent.areaMapping.put(areaPosition, worldMap.get(areaPosition));
                }
            }
        }


        // Only display debug info every 2 seconds
        if (agentSightComponent.timeSinceLastDebugOutput >= 2) {
            agentSightComponent.timeSinceLastDebugOutput = 0;
            System.out.print(entity.getComponent(NameComponent.class).getName() + " can see the following objects: ");
            if (!agentSightComponent.areaMapping.isEmpty()) {
                for (Entity e : agentSightComponent.areaMapping.values()) {
                    float distance = agentPosition.dst(worldMap.inverse().get(e));
                    System.out.print(e.getComponent(NameComponent.class).getName() + " (dst=" + distance + ") ");
                }
                System.out.println();
            }
            else {
                System.out.println("None");
            }
        }
        else {
            agentSightComponent.timeSinceLastDebugOutput += deltaTime;
        }

    }
}
