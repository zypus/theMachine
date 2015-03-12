package com.the.machine.framework.components;

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
        if (newAngularVelocity > maxAngularVelocity) {
            this.angularVelocity = maxAngularVelocity;
        }
        else if (newAngularVelocity < -maxAngularVelocity) {
            this.angularVelocity = -maxAngularVelocity;
        }
        else {
            this.angularVelocity = newAngularVelocity;
        }
    }

    public void setMaxAngularVelocity(float maxAngularVelocity) {
        this.maxAngularVelocity = maxAngularVelocity;
    }

    public float getAngularVelocity() { return angularVelocity; }
    public float getMaxAngularVelocity() { return maxAngularVelocity; }
}
