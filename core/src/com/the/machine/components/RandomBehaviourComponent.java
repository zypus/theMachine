package com.the.machine.components;

import com.the.machine.framework.components.AbstractComponent;

//Note: This seems to not be used

/**
 * Created by Frans on 12-3-2015.
 */
public class RandomBehaviourComponent extends AbstractComponent {
    private float timeSinceLastRandomBehaviour = 0;
    private float timeBetweenRandomBehaviours = 2;  // in seconds

    /*
     * Getters and setters
     */
    public float getTimeBetweenRandomBehaviours() {
        return timeBetweenRandomBehaviours;
    }

    public RandomBehaviourComponent setTimeBetweenRandomBehaviours(float timeBetweenRandomBehaviours) {
        this.timeBetweenRandomBehaviours = timeBetweenRandomBehaviours;
        return this;
    }

    public float getTimeSinceLastRandomBehaviour() {
        return timeSinceLastRandomBehaviour;
    }

    public RandomBehaviourComponent setTimeSinceLastRandomBehaviour(float timeSinceLastRandomBehaviour) {
        this.timeSinceLastRandomBehaviour = timeSinceLastRandomBehaviour;
        return this;
    }

    public RandomBehaviourComponent increaseTimeSinceLastRandomBehaviourWith(float delta) {
        timeSinceLastRandomBehaviour += delta;
        return this;
    }
}
