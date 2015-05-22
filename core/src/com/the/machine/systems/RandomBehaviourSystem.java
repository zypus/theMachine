package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.MapGroundComponent;
import com.the.machine.components.RandomBehaviourComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.TransformComponent;
import lombok.EqualsAndHashCode;

/**
 * Created by Frans on 12-3-2015.
 */
@EqualsAndHashCode
public class RandomBehaviourSystem extends IteratingSystem {
    private transient ComponentMapper<RandomBehaviourComponent> randomBehaviours = ComponentMapper.getFor(RandomBehaviourComponent.class);
    private ImmutableArray<Entity> groundComponentEntities;

    public RandomBehaviourSystem() {
        super(Family.one(RandomBehaviourComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        groundComponentEntities = null;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        groundComponentEntities = engine.getEntitiesFor(Family.one(MapGroundComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        RandomBehaviourComponent randomBehaviourComponent = randomBehaviours.get(entity);

        randomBehaviourComponent.increaseTimeSinceLastRandomBehaviourWith(-deltaTime);

        if (randomBehaviourComponent.getTimeSinceLastRandomBehaviour() < 0) {
            // Time for a new random behaviour
            float newAngularVelocity = (float) ((Math.random() * 180) - 90);
            float newSpeed = (float) (Math.random() * 0.5);

//            BehaviourComponent behaviourComponent = new BehaviourComponent(
//                    new AngularVelocityComponent(newAngularVelocity, 360),
//                    new VelocityComponent().setVelocities(newSpeed)
//            );
//            entity.add(behaviourComponent);

            randomBehaviourComponent.increaseTimeSinceLastRandomBehaviourWith(randomBehaviourComponent.getTimeBetweenRandomBehaviours());

            // If the entity is outside the 4x4 square around the center, teleport the agent to (0, 0
            Vector2 entityPosition = entity.getComponent(TransformComponent.class).get2DPosition();

            if (groundComponentEntities.size() > 0) {
                /*
                 * The entity that has the MapGroundComponent controls the size of the map with its DimensionComponent.
                 * If the entity we are processing here is outside of this dimensionComponent, move it back to the center.
                 */
                DimensionComponent groundDimension = groundComponentEntities.get(0).getComponent(DimensionComponent.class);
                float groundWidth = groundDimension.getWidth();
                float groundHeight = groundDimension.getHeight();
                if ( Math.abs(entityPosition.x) >= 0.5*groundWidth || Math.abs(entityPosition.y) >= 0.5*groundHeight) {
                    entity.getComponent(TransformComponent.class).set2DPosition(new Vector2(0, 0));
                }
            }
        }
    }
}
