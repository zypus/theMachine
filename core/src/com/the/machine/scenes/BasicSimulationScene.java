package com.the.machine.scenes;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Bits;
import com.the.machine.components.AgentSightComponent;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.RandomBehaviourComponent;
import com.the.machine.framework.SceneBuilder;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.*;
import com.the.machine.framework.components.physics.ColliderComponent;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.systems.rendering.CameraRenderSystem;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.systems.AgentSightSystem;
import com.the.machine.systems.BehaviourSystem;
import com.the.machine.systems.MovementSystem;
import com.the.machine.systems.RandomBehaviourSystem;
import com.the.machine.systems.RotationSystem;
import com.the.machine.systems.WorldMappingSystem;

import java.util.Random;

/**
 * Created by Frans on 12-3-2015.
 */
public class BasicSimulationScene implements SceneBuilder {
    private static Random random = new Random();

    @Override
    public void createScene(World world) {
        // The systems which act in this scene
        world.addSystem(new CameraRenderSystem(), AssetLoadingFinishedEvent.class);
        world.addSystem(new MovementSystem());
        world.addSystem(new RotationSystem());
        world.addSystem(new BehaviourSystem());
        world.addSystem(new RandomBehaviourSystem());
        world.addSystem(new AgentSightSystem(1));
        world.addSystem(new WorldMappingSystem(2)); // Must have a higher priority than AgentSightSystem

        createCamera(world);
        createGround(world);

        // Create the guards
        createGuard(world, "guard1");
        createGuard(world, "guard2");
        createGuard(world, "guard3");

        createArea(world, AreaComponent.AreaType.TOWER, new TransformComponent().set2DPosition(new Vector2(-1, -1)).setScale(0.5f));
        createArea(world, AreaComponent.AreaType.DOOR_OPEN, new TransformComponent().set2DPosition(new Vector2(1.5f, 1.5f)).setScale(1, 0.3f, 1));
        createArea(world, AreaComponent.AreaType.TARGET, new TransformComponent().set2DPosition(new Vector2(-1, 1)).setScale(0.5f));
    }

    private void createCamera(World w) {
        Entity camera = new Entity();
        CameraComponent cameraComponent = new CameraComponent();
        Bits mask = BitBuilder.all(32)
                .c(0)
                .get();
        cameraComponent.setCullingMask(mask);
        cameraComponent.setProjection(CameraComponent.Projection.ORTHOGRAPHIC);
        cameraComponent.setZoom(0.01f);
        camera.add(cameraComponent);

        TransformComponent transformComponent = new TransformComponent().setPosition(new Vector3(0, 0, 5));
        camera.add(transformComponent);             // The transform component allows moving and rotating the camera
        camera.add(new NameComponent().setName("Camera" + random.nextInt()));
        w.addEntity(camera);
    }

    private void createGuard(World w, String assetName) {
        Entity guard = new Entity();

        guard.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch(assetName, TextureRegion.class)).setSortingOrder(2));
        guard.add(new TransformComponent().set2DPosition(new Vector2(0, 0)).setScale(0.2f).setZ(2));
        guard.add(new NameComponent().setName("Guard" + random.nextInt()));
        guard.add(new AgentSightComponent());
        guard.add(new RandomBehaviourComponent());
        guard.add(new ColliderComponent().add(new ColliderComponent.Collider()));

        w.addEntity(guard);
    }

    private void createGround(World w) {
        Entity ground = new Entity();
        ground.add(new AreaComponent().setType(AreaComponent.AreaType.GROUND));
        ground.add(new SpriteRenderComponent().setTextureRegion(AreaComponent.AreaType.GROUND.getTextureAsset()).setSortingLayer("Default"));
        ground.add(new DimensionComponent().setDimension(4, 4));

        w.addEntity(ground);
    }

    private void createArea(World w, AreaComponent.AreaType type, TransformComponent transformComponent) {
        Entity area = new Entity();

        area.add(transformComponent);
        area.add(new NameComponent().setName(type.name() + random.nextInt()));
        area.add(new AreaComponent().setType(type));
        area.add(new SpriteRenderComponent().setTextureRegion(type.getTextureAsset()));

        w.addEntity(area);
    }
}
