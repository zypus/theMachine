package com.the.machine.components;

import com.the.machine.framework.components.AbstractComponent;

/**
 * Created by Frans on 12-3-2015.
 */
public class RandomBehaviourComponent extends AbstractComponent {
    public float timeBetweenRandomBehaviours = 1;  // second
    public float timeSinceLastRandomBehaviour = 0;
}
