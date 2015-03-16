package com.the.machine.components;

import com.the.machine.framework.components.AbstractComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Frans on 12-3-2015.
 */
public class BehaviourComponent extends AbstractComponent {
    private List behaviours = new ArrayList<AbstractComponent>();

    public BehaviourComponent(AbstractComponent... newBehaviours) {
        behaviours = Arrays.asList(newBehaviours);
    }

    public List<AbstractComponent> getBehaviours() {
        return behaviours;
    }
}
