package com.the.machine.scenes;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Bits;
import com.the.machine.framework.SceneBuilder;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.*;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.systems.MovementSystem;
import com.the.machine.framework.systems.rendering.CameraRenderSystem;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.EntityUtilities;

/**
 * The Scene that is shown when the World is created
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/02/15
 */
public class SubEntityTestScene
		implements SceneBuilder {
	@Override
	public void createScene(World world) {
        // The systems which act in this scene
		world.addSystem(new CameraRenderSystem(), AssetLoadingFinishedEvent.class);
        world.addSystem(new MovementSystem());

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

        // badlogicImageEntity and badlogicImageEntity2 (both are only textures)

        /*
         * badLogicImageEntity(2) has the following components
         *  - layerComponent
         *  - spriteRenderComponent
         *  - transformComponent
         *  - nameComponent
         */
        Entity badlogicImageEntity = new Entity();
        Asset<TextureRegion> textureRegion = Asset.fetch("badlogic", TextureRegion.class);
		badlogicImageEntity.add(new LayerComponent(BitBuilder.none(32)
											  .s(1)
											  .get()));
		badlogicImageEntity.add(new SpriteRenderComponent().setTextureRegion(textureRegion)
											.setSortingLayer("Default"));
		badlogicImageEntity.add(new TransformComponent().setPosition(new Vector3(0, 0, 0))
										 .setZRotation(0)
										 .setScale(1f));
		badlogicImageEntity.add(new NameComponent().setName("Badlogic1"));
        badlogicImageEntity.add(new DirectionComponent().setDirection(new Vector3(
                (float) 0.1,
                (float) 0.05,
                (float) 0)));
		world.addEntity(badlogicImageEntity);

		Entity badlogicImageEntity2 = new Entity();
		badlogicImageEntity2.add(new LayerComponent(BitBuilder.none(32)
											  .s(1)
											  .get()));
		badlogicImageEntity2.add(new SpriteRenderComponent().setTextureRegion(textureRegion)
											.setSortingLayer("Default"));
		badlogicImageEntity2.add(new TransformComponent().setPosition(new Vector3(0, 1, 0))
										 .setZRotation(0)
										 .setScale(1f));
		badlogicImageEntity2.add(new NameComponent().setName("Badlogic2"));
		EntityUtilities.relate(badlogicImageEntity, badlogicImageEntity2);
        world.addEntity(badlogicImageEntity2);
	}
}
