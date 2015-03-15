package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AgentSightComponent;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.VelocityComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.TransformComponent;

/**
 * Created by Frans on 15-3-2015.
 */
public class AgentSightSystem extends IteratingSystem {
    //private transient ComponentMapper<AreaComponent> areas = ComponentMapper.getFor(AreaComponent.class);
    // TODO: Performance for large maps can be improved by creating a central, discrete map of the areatypes

    public AgentSightSystem() {
        super(Family.all(
                AgentSightComponent.class,  // To store the sight information
                TransformComponent.class    // The agent needs a location and a direction to be able to see things
        ).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        AgentSightComponent agentSightComponent = entity.getComponent(AgentSightComponent.class);
        TransformComponent agentTransformComponent = entity.getComponent(TransformComponent.class);

        agentSightComponent.areaMapping.clear();

        for (Entity areaInView : getWorld().getEntitiesFor(Family.all(
                AreaComponent.class,
                TransformComponent.class
        ).get())) {
            // TODO: Make agents only view items that are in their view angle

            if (areaInView != entity) {
                AreaComponent areaComponent = areaInView.getComponent(AreaComponent.class);
                TransformComponent areaTransformComponent = areaInView.getComponent(TransformComponent.class);

                Vector2 agentPosition = agentTransformComponent.get2DPosition();
                Vector2 areaPosition = areaTransformComponent.get2DPosition();
                float maximumSightDistance = agentSightComponent.maximumSightDistance;
                if (agentPosition.dst(areaPosition) <= maximumSightDistance) {
                    agentSightComponent.areaMapping.put(areaPosition, areaComponent.getType());
                }
            }
        }

        if (!agentSightComponent.areaMapping.isEmpty()) {
            System.out.println(entity.getComponent(NameComponent.class).getName() + " can see the following objects: ");
            System.out.println(agentSightComponent.areaMapping);
        }
    }
}
