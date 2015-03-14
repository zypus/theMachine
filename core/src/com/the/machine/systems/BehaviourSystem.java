package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.the.machine.components.ActionComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.VelocityComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.AbstractComponent;
import com.the.machine.components.AngularVelocityComponent;

/**
 * Created by Frans on 12-3-2015.
 */
public class BehaviourSystem extends IteratingSystem {
    private transient ComponentMapper<BehaviourComponent> behaviours = ComponentMapper.getFor(BehaviourComponent.class);

    public BehaviourSystem() {
        super(Family.one(BehaviourComponent.class).get());
    }


    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        /*
         * The BehaviourSystem controls the
         *  - velocityComponent
         *  - angularVelocityComponent
         *  - action
         * of the agent
         */

        BehaviourComponent behaviour = behaviours.get(entity);

        for (AbstractComponent behaviourComponent : behaviour.getBehaviours()) {
            if (behaviourComponent.getClass() == VelocityComponent.class) {
                entity.remove(VelocityComponent.class);
                entity.add(behaviourComponent);
            }
            else if (behaviourComponent.getClass() == AngularVelocityComponent.class) {
                entity.remove(AngularVelocityComponent.class);
                entity.add(behaviourComponent);
            }
            else if (behaviourComponent.getClass() == ActionComponent.class) {
                entity.remove(AngularVelocityComponent.class);
                entity.add(behaviourComponent);
            }
        }

        entity.remove(BehaviourComponent.class);
    }
}
