package com.the.machine.components;

import com.the.machine.framework.components.AbstractComponent;

/**
 * Created by Frans on 12-3-2015.
 */
public class AngularVelocityComponent extends AbstractComponent {
    private float angularVelocity;      // In degrees per second
    private float maxAngularVelocity;

    public AngularVelocityComponent(float angularVelocity, float maxAngularVelocity) {
        this.angularVelocity = angularVelocity;
        this.maxAngularVelocity = maxAngularVelocity;
    }

    public void setAngularVelocity(float newAngularVelocity) {
        // Make sure that
        // -maxVelocity <= velocity <= maxVelocity
        float maxAngularVelocityAbs = Math.abs(maxAngularVelocity);
        this.angularVelocity = Math.min(newAngularVelocity, maxAngularVelocityAbs);
        this.angularVelocity = Math.max(newAngularVelocity, -maxAngularVelocityAbs);
    }

    public void setMaxAngularVelocity(float maxAngularVelocity) {
        this.maxAngularVelocity = maxAngularVelocity;
    }

    public float getAngularVelocity() { return angularVelocity; }
    public float getMaxAngularVelocity() { return maxAngularVelocity; }
}
