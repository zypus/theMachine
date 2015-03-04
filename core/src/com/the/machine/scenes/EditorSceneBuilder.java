package com.the.machine.scenes;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Bits;
import com.the.machine.framework.SceneBuilder;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.CanvasComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.IgnoredComponent;
import com.the.machine.framework.components.InspectorComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.WorldTreeComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.components.canvasElements.TableComponent;
import com.the.machine.framework.components.canvasElements.TreeComponent;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.events.basic.ResizeEvent;
import com.the.machine.framework.systems.canvas.CanvasElementSystem;
import com.the.machine.framework.systems.canvas.CanvasSystem;
import com.the.machine.framework.systems.canvas.TableCellSystem;
import com.the.machine.framework.systems.canvas.TableSystem;
import com.the.machine.framework.systems.canvas.TreeNodeSystem;
import com.the.machine.framework.systems.canvas.TreeSystem;
import com.the.machine.framework.systems.editor.InspectorSystem;
import com.the.machine.framework.systems.editor.WorldTreeSystem;
import com.the.machine.framework.systems.rendering.CameraRenderSystem;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.EntityUtilities;
import com.the.machine.framework.utility.Enums;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/02/15
 */
public class EditorSceneBuilder
		implements SceneBuilder {

	@Override
	public void createScene(World world) {
		world.addSystem(new CameraRenderSystem(), AssetLoadingFinishedEvent.class);
		world.addSystem(new CanvasSystem(), ResizeEvent.class);
		world.addSystem(new CanvasElementSystem());
		world.addSystem(new TableSystem(), ResizeEvent.class);
		world.addSystem(new TableCellSystem());
		world.addSystem(new TreeSystem());
		world.addSystem(new TreeNodeSystem());
		world.addSystem(new WorldTreeSystem());
		world.addSystem(new InspectorSystem());

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

		Entity uiCamera = new Entity();
		CameraComponent uiCameraComponent = new CameraComponent();
		uiCameraComponent.setCullingMask(BitBuilder.none(32)
												   .s(0)
												   .get());
		uiCameraComponent.setProjection(CameraComponent.Projection.ORTHOGRAPHIC);
		uiCameraComponent.setZoom(1f);
		uiCameraComponent.setDepth(100);
		uiCameraComponent.setClearFlag(0);
		uiCameraComponent.setOrigin(new Vector2(0f, 0f));
		uiCameraComponent.getClippingPlanes()
						 .setNear(-300f);
		uiCamera.add(uiCameraComponent);
		uiCamera.add(new TransformComponent().setPosition(0, 0, 0));
		uiCamera.add(new NameComponent().setName("UI Camera"));
		world.addEntity(uiCamera);

		Asset<TextureRegion> textureRegion = Asset.fetch("badlogic", TextureRegion.class);

		Entity test = new Entity();
		SpriteRenderComponent spriteRenderComponent = new SpriteRenderComponent().setTextureRegion(textureRegion)
																				 .setSortingLayer("Default");
		test.add(new LayerComponent(BitBuilder.none(32)
											  .s(1)
											  .get()));
		test.add(spriteRenderComponent);
		TransformComponent spriteTransform = new TransformComponent().setPosition(new Vector3(0, 0, 0))
																	 .setZRotation(0)
																	 .setScale(1f);
		test.add(spriteTransform);
		SubEntityComponent subEntityComponent = new SubEntityComponent();
		test.add(subEntityComponent);
		test.add(new NameComponent().setName("Badlogic"));
		world.addEntity(test);

		Entity canvas = new Entity();
		canvas.add(new CanvasComponent());
		canvas.add(new CanvasElementComponent());
		canvas.add(new TransformComponent());
		canvas.add(new DimensionComponent());
		canvas.add(new LayerComponent(BitBuilder.none(32)
												.s(0)
												.get()));
		canvas.add(new NameComponent().setName("Canvas"));
		world.addEntity(canvas);

		// WORLD TREE TEST

		Entity table = new Entity();
		table.add(new CanvasElementComponent());
		table.add(new TableComponent().setFillParent(true)
									  .setHorizontalAlignment(Enums.HorizontalAlignment.LEFT)
									  .setVerticalAlignment(Enums.VerticalAlignment.BOTTOM));
		table.add(new TransformComponent());
		table.add(new DimensionComponent());
		table.add(new IgnoredComponent());
		table.add(new NameComponent().setName("Editor Table"));
		EntityUtilities.relate(canvas, table);
		world.addEntity(table);

		Entity tree = new Entity();
		tree.add(new TransformComponent().setPosition(100, 100, 0));
		tree.add(new DimensionComponent().setDimension(100, 200)
										 .setOrigin(0, 0));
		tree.add(new CanvasElementComponent());
		tree.add(new TreeComponent());
		tree.add(new TableCellComponent().setFillY(1)
										 .setExpandY(1)
										 .setExpandX(1)
										 .setHorizontalAlignment(Enums.HorizontalAlignment.LEFT));
		tree.add(new WorldTreeComponent());
		tree.add(new IgnoredComponent());
		tree.add(new NameComponent().setName("World Tree"));
		world.addEntity(tree);
		EntityUtilities.relate(table, tree);

		Entity transformDebug = new Entity();
		transformDebug.add(new TransformComponent());
		transformDebug.add(new DimensionComponent());
		transformDebug.add(new CanvasElementComponent());
		transformDebug.add(new InspectorComponent());
		transformDebug.add(new TableCellComponent());
		transformDebug.add(new TableComponent());
		EntityUtilities.relate(table, transformDebug);
		world.addEntity(transformDebug);
	}
}
