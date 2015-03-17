package com.the.machine.scenes;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Bits;
import com.the.machine.components.*;
import com.the.machine.framework.SceneBuilder;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.*;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.systems.rendering.CameraRenderSystem;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.EntityUtilities;
import com.the.machine.systems.*;

/**
 * Created by Frans on 12-3-2015.
 */
public class BasicSimulationScene implements SceneBuilder {

    @Override
    public void createScene(World world) {
        // The systems which act in this scene
        world.addSystem(new CameraRenderSystem(), AssetLoadingFinishedEvent.class);
        world.addSystem(new MovementSystem());
        world.addSystem(new RotationSystem());
        world.addSystem(new BehaviourSystem());
        world.addSystem(new RandomBehaviourSystem());
        world.addSystem(new WorldMappingSystem(2)); // Must have a higher priority than AgentSightSystem
        world.addSystem(new AgentSightSystem(1));

        // Camera
        Entity camera = new Entity();
        CameraComponent cameraComponent = new CameraComponent();
        Bits mask = BitBuilder.all(32)
                .c(0)
                .get();
        cameraComponent.setCullingMask(mask);
        cameraComponent.setProjection(CameraComponent.Projection.PERSPECTIVE);
        cameraComponent.setZoom(0.01f);
        camera.add(cameraComponent);

        TransformComponent transformComponent = new TransformComponent().setPosition(new Vector3(0, 0, 5));
        camera.add(transformComponent);             // The transform component allows moving and rotating the camera
        camera.add(new NameComponent().setName("Main Camera"));
        world.addEntity(camera);

        // Create the guards
        Entity guard1 = new Entity();
        Entity guard2 = new Entity();
        Entity guard3 = new Entity();

        guard1.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch("guard1", TextureRegion.class)).setSortingOrder(-2));
        guard1.add(new TransformComponent().set2DPosition(new Vector2(0, 0)).setScale(0.2f));
        guard1.add(new NameComponent().setName("Guard1"));
        guard1.add(new AgentSightComponent());
        guard1.add(new RandomBehaviourComponent());

        guard2.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch("guard2", TextureRegion.class)).setSortingOrder(-2));
        guard2.add(new TransformComponent().set2DPosition(new Vector2(0, 0)).setScale(0.2f));
        guard2.add(new NameComponent().setName("Guard2"));
        guard2.add(new AgentSightComponent());
        guard2.add(new RandomBehaviourComponent());

        guard3.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch("guard3", TextureRegion.class)).setSortingOrder(-2));
        guard3.add(new TransformComponent().set2DPosition(new Vector2(0, 0)).setScale(0.2f));
        guard3.add(new NameComponent().setName("Guard3"));
        guard3.add(new AgentSightComponent());
        guard3.add(new RandomBehaviourComponent());

        world.addEntity(guard1);
        world.addEntity(guard2);
        world.addEntity(guard3);

        // And create some entities which can be seen by the guards
        Entity tower1 = new Entity();
        Entity door_open_1 = new Entity();
        Entity target = new Entity();

        tower1.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch("tower", TextureRegion.class)));
        tower1.add(new TransformComponent().set2DPosition(new Vector2(-1, -1)).setScale(0.5f));
        tower1.add(new NameComponent().setName("Tower1"));
        tower1.add(new AreaComponent().setType(AreaComponent.AreaType.TOWER));

        door_open_1.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch("door_open", TextureRegion.class)));
        door_open_1.add(new TransformComponent().set2DPosition(new Vector2(1.5f, 1.5f)).setScale(1, 0.3f, 1));
        door_open_1.add(new NameComponent().setName("DoorOpen1"));
        door_open_1.add(new AreaComponent().setType(AreaComponent.AreaType.DOOR_OPEN));

        target.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch("target", TextureRegion.class)));
        target.add(new TransformComponent().set2DPosition(new Vector2(1, -.5f)).setScale(2f, 2, 1));
        target.add(new NameComponent().setName("Target1"));
        target.add(new AreaComponent().setType(AreaComponent.AreaType.TARGET));

        world.addEntity(tower1);
        world.addEntity(door_open_1);
        world.addEntity(target);
    }
}
