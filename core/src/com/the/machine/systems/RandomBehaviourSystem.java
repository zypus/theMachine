package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.RandomBehaviourComponent;
import com.the.machine.components.VelocityComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.components.AngularVelocityComponent;

/**
 * Created by Frans on 12-3-2015.
 */
public class RandomBehaviourSystem extends IteratingSystem {
    private transient ComponentMapper<RandomBehaviourComponent> randomBehaviours = ComponentMapper.getFor(RandomBehaviourComponent.class);

    public RandomBehaviourSystem() {
        super(Family.one(RandomBehaviourComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        RandomBehaviourComponent randomBehaviourComponent = randomBehaviours.get(entity);

        randomBehaviourComponent.timeSinceLastRandomBehaviour -= deltaTime;

        if (randomBehaviourComponent.timeSinceLastRandomBehaviour < 0) {
            // Time for a new random behaviour
            System.out.println("New behaviour");
            float newAngle = (float) ((Math.random() * 45) - 22.5);

            float newSpeed = (float) (Math.random() - 0.5);

            BehaviourComponent behaviourComponent = new BehaviourComponent(
                    new AngularVelocityComponent(newAngle, 360),
                    new VelocityComponent().setVelocity(newSpeed)
            );
            entity.add(behaviourComponent);

            randomBehaviourComponent.timeSinceLastRandomBehaviour += randomBehaviourComponent.timeBetweenRandomBehaviours;
        }
    }
}
