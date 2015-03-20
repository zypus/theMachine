package com.the.machine.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.ObservableComponent;
import com.the.machine.framework.components.SpriteRenderComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Frans on 15-3-2015.
 */
public class AgentSightComponent extends ObservableComponent {
    public Map<Vector2, Entity> areaMapping = new HashMap<>();
    private float degreesOfSight;
    private float maximumSightDistance;
    private float minimumSightDistance;


    public float getMaximumSightDistance() {
        return maximumSightDistance;
    }

    public float getMinimumSightDistance() {
        return minimumSightDistance;
    }

    public float getDegreesOfSight() {
        return degreesOfSight;
    }

    public List<Vector2> areasBeingSeen = new ArrayList<>();   // Only for debugging

    public AgentSightComponent setMaximumSightDistance(float maximumSightDistance) {
        this.maximumSightDistance = maximumSightDistance;
        return this;
    }

    public AgentSightComponent setMinimumSightDistance(float minimumSightDistance) {
        this.minimumSightDistance = minimumSightDistance;
        return this;
    }

    public AgentSightComponent setDegreesOfSight(float degreesOfSight) {
        this.degreesOfSight = degreesOfSight;
        return this;
    }

    public AgentSightComponent() {
        minimumSightDistance = 1f;
        maximumSightDistance = 2f;
        degreesOfSight = 90;
    }

    // For debugging
    public float timeSinceLastDebugOutput = 0;
}
