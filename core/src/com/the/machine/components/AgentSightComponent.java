package com.the.machine.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.the.machine.framework.components.ObservableComponent;

import java.util.HashMap;

/**
 * Created by Frans on 15-3-2015.
 */
public class AgentSightComponent extends ObservableComponent {
    public BiMap<Vector2, Entity> areaMapping;

    public float getMaximumSightDistance() {
        return maximumSightDistance;
    }

    public AgentSightComponent setMaximumSightDistance(float maximumSightDistance) {
        this.maximumSightDistance = maximumSightDistance;
        return this;
    }

    //public float degreesOfSight;
    public float maximumSightDistance;

    public AgentSightComponent() {
        areaMapping = HashBiMap.create();
        maximumSightDistance = 1;
    }

    // For debugging
    public float timeSinceLastDebugOutput = 0;
}
