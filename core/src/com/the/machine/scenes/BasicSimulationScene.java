package com.the.machine.scenes;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Bits;
import com.the.machine.components.*;
import com.the.machine.events.AudioEvent;
import com.the.machine.framework.SceneBuilder;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.*;
import com.the.machine.framework.components.physics.ColliderComponent;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.events.input.TouchDownEvent;
import com.the.machine.framework.events.input.TouchDraggedEvent;
import com.the.machine.framework.events.input.TouchUpEvent;
import com.the.machine.framework.systems.DelayedRemovalSystem;
import com.the.machine.framework.systems.rendering.CameraRenderSystem;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.systems.*;

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
        world.addSystem(new AgentSightSystem(1));
        world.addSystem(new WorldMappingSystem(2)); // Must have a higher priority than AgentSightSystem
        world.addSystem(new DelayedRemovalSystem());
        world.addSystem(new DraggingSystem(), TouchDownEvent.class, TouchDraggedEvent.class, TouchUpEvent.class);

        createCamera(world);
        createGround(world);

        for (int i = 0; i < 2; i++)
            createGuard(world, "guard3");

        createArea(world, AreaComponent.AreaType.TOWER,     new TransformComponent().set2DPosition(new Vector2(-1, -1))    .setScale(0.5f));
        createArea(world, AreaComponent.AreaType.DOOR_OPEN, new TransformComponent().set2DPosition(new Vector2(1.5f, 1.5f)).setScale(1, 0.3f, 1));
        createArea(world, AreaComponent.AreaType.TARGET, new TransformComponent().set2DPosition(new Vector2(-1.5f, 2))     .setScale(5, 3, 1));
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
        camera.add(transformComponent);
        camera.add(new NameComponent().setName("Camera" + random.nextInt()));
        camera.add(new SelectorComponent());
        w.addEntity(camera);
    }

    private void createGuard(World w, String assetName) {
        Entity guard = new Entity();

        guard.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch(assetName, TextureRegion.class)).setSortingOrder(2));
        guard.add(new TransformComponent().set2DPosition(new Vector2(0, 0)).setScale(1).setZ(2));
        guard.add(new NameComponent().setName("Guard" + random.nextInt()));
        guard.add(new AgentSightComponent());
        guard.add(new RandomBehaviourComponent());
        guard.add(new ColliderComponent().add(new ColliderComponent.Collider()));
        guard.add(new DraggableComponent());
        guard.add(new DimensionComponent().setDimension(0.2f, 0.2f));

        w.addEntity(guard);
    }

    private void createGround(World w) {
        Entity ground = new Entity();
        ground.add(new MapGroundComponent());
        ground.add(new AreaComponent().setType(AreaComponent.AreaType.GROUND));
        //ground.add(new SpriteRenderComponent().setTextureRegion(AreaComponent.AreaType.GROUND.getTextureAsset()).setSortingLayer("Default"));
        ground.add(new DimensionComponent().setDimension(8, 8));
        ground.add(new WorldMapComponent());

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
