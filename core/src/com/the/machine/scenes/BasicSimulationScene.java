package com.the.machine.scenes;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Bits;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.VelocityComponent;
import com.the.machine.framework.SceneBuilder;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.*;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.systems.rendering.CameraRenderSystem;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.EntityUtilities;
import com.the.machine.systems.BehaviourSystem;
import com.the.machine.systems.MovementSystem;
import com.the.machine.systems.RotationSystem;

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

		/*
		 * Create the entities
		 *
		 * Atm it has a
		 *   - main camera
		 *   - badlogicImageEntity
		 */

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



        Entity badlogicImageEntity1 = new Entity();
        Asset<TextureRegion> textureRegion = Asset.fetch("badlogic", TextureRegion.class);
        badlogicImageEntity1.add(new LayerComponent(BitBuilder.none(32)
                .s(1)
                .get()));
        badlogicImageEntity1.add(new SpriteRenderComponent().setTextureRegion(textureRegion)
                .setSortingLayer("Default"));
        badlogicImageEntity1.add(new TransformComponent().setPosition(new Vector3(0, 0, 0))
                .setZRotation(0)
                .setScale(0.2f));
        badlogicImageEntity1.add(new NameComponent().setName("Badlogic1"));
        badlogicImageEntity1.add(new BehaviourComponent(
                new VelocityComponent().setVelocity(0.5f),
                new AngularVelocityComponent(90, 180)
        ));


        Entity badlogicImageEntity2 = new Entity();
        // Use same texture as the first badlogicImageEntity
        badlogicImageEntity2.add(new SpriteRenderComponent().setTextureRegion(textureRegion)
                .setSortingLayer("Default"));
        badlogicImageEntity2.add(new TransformComponent().setPosition(new Vector3(0, 0, 0))
                .setZRotation(0)
                .setScale(0.2f));
        badlogicImageEntity2.add(new NameComponent().setName("Badlogic2"));
        badlogicImageEntity2.add(new BehaviourComponent(
                new VelocityComponent().setVelocity(1f),
                new AngularVelocityComponent(-90, 360)
        ));

        world.addEntity(badlogicImageEntity1);
        world.addEntity(badlogicImageEntity2);
    }
}
