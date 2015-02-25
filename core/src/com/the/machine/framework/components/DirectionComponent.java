package com.the.machine.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Frans on 25-2-2015.
 */
public class DirectionComponent extends ObservableComponent {
    private Vector3 direction = new Vector3(0, 0, 0);

    public DirectionComponent setDirection(float x, float y, float z) {
        direction.set(x,y,z);
        setChanged();
        return this;
    }

    public DirectionComponent setDirection(Vector3 new_direction) {
        direction.set(new_direction);
        setChanged();
        return this;
    }

    public Vector3 getDirection() { return new Vector3(direction); }
    public float getXDirection() { return direction.x; }
    public float getYDirection() { return direction.y; }
    public float getZDirection() { return direction.z; }
}
