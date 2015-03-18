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

    public float getMaximumSightDistance() {
        return maximumSightDistance;
    }
    public List<Vector2> areasBeingSeen = new ArrayList<>();   // Only for debugging
    public static SpriteRenderComponent whiteSprite = new SpriteRenderComponent().setTextureRegion(Asset.fetch("white", TextureRegion.class));// new SpriteRenderComponent().setTextureRegion(Asset.fetch("white", TextureRegion.class)).setSortingOrder(-1); // Also for debugging

    public AgentSightComponent setMaximumSightDistance(float maximumSightDistance) {
        this.maximumSightDistance = maximumSightDistance;
        return this;
    }

    public float degreesOfSight;
    public float maximumSightDistance;

    public AgentSightComponent() {
        maximumSightDistance = 1f;
        degreesOfSight = 10;
    }

    // For debugging
    public float timeSinceLastDebugOutput = 0;
}
