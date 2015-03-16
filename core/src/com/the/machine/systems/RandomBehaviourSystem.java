package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.RandomBehaviourComponent;
import com.the.machine.components.VelocityComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.components.AngularVelocityComponent;
import com.the.machine.framework.components.TransformComponent;

import java.awt.*;

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

        randomBehaviourComponent.increaseTimeSinceLastRandomBehaviourWith(-deltaTime);

        if (randomBehaviourComponent.getTimeSinceLastRandomBehaviour() < 0) {
            // Time for a new random behaviour
            float newAngularVelocity = (float) ((Math.random() * 180) - 90);
            float newSpeed = (float) (Math.random() * 0.5);

            BehaviourComponent behaviourComponent = new BehaviourComponent(
                    new AngularVelocityComponent(newAngularVelocity, 360),
                    new VelocityComponent().setVelocity(newSpeed)
            );
            entity.add(behaviourComponent);

            randomBehaviourComponent.increaseTimeSinceLastRandomBehaviourWith(randomBehaviourComponent.getTimeBetweenRandomBehaviours());

            // If the entity is outside the 4x4 square around the center, teleport the agent to (0, 0
            Vector2 entityPosition = entity.getComponent(TransformComponent.class).get2DPosition();
            if (!new Rectangle(-2, -2, 4, 4).contains(entityPosition.x, entityPosition.y)) {
                entity.getComponent(TransformComponent.class).set2DPosition(new Vector2(0, 0));
            }
        }
    }
}
