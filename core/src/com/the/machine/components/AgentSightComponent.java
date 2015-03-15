package com.the.machine.components;

import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AreaComponent;
import com.the.machine.framework.components.ObservableComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frans on 15-3-2015.
 */
public class AgentSightComponent extends ObservableComponent {
    // Use the AreaTypes from the AreaComponent
    public Map<Vector2, AreaComponent.AreaType> areaMapping;
    //public float degreesOfSight;
    public float maximumSightDistance = 1;

    public AgentSightComponent() {
        areaMapping = new HashMap<>();
    }
}
