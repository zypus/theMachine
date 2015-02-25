package com.the.machine.scenes;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Bits;
import com.the.machine.framework.SceneBuilder;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.systems.rendering.CameraRenderSystem;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.EntityUtilities;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/02/15
 */
public class SubEntityTestScene
		implements SceneBuilder {
	@Override
	public void createScene(World world) {
		world.addSystem(new CameraRenderSystem(), AssetLoadingFinishedEvent.class);

		// entities
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
		camera.add(transformComponent);
		camera.add(new NameComponent().setName("Main Camera"));
		world.addEntity(camera);

		Asset<TextureRegion> textureRegion = Asset.fetch("badlogic", TextureRegion.class);

		Entity test = new Entity();
		test.add(new LayerComponent(BitBuilder.none(32)
											  .s(1)
											  .get()));
		test.add(new SpriteRenderComponent().setTextureRegion(textureRegion)
											.setSortingLayer("Default"));
		test.add(new TransformComponent().setPosition(new Vector3(0, 0, 0))
										 .setZRotation(0)
										 .setScale(1f));
		test.add(new NameComponent().setName("Badlogic1"));
		world.addEntity(test);

		Entity test2 = new Entity();
		test2.add(new LayerComponent(BitBuilder.none(32)
											  .s(1)
											  .get()));
		test2.add(new SpriteRenderComponent().setTextureRegion(textureRegion)
											.setSortingLayer("Default"));
		test2.add(new TransformComponent().setPosition(new Vector3(0, 1, 0))
										 .setZRotation(0)
										 .setScale(1f));
		test2.add(new NameComponent().setName("Badlogic2"));
		EntityUtilities.relate(test, test2);
		world.addEntity(test2);
	}
}
