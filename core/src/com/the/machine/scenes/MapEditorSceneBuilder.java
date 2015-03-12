package com.the.machine.scenes;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Bits;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.ControlComponent;
import com.the.machine.components.DirectionalVelocityComponent;
import com.the.machine.events.MapEditorLoadEvent;
import com.the.machine.events.MapEditorSaveEvent;
import com.the.machine.framework.SceneBuilder;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.CanvasComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.ButtonComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.components.canvasElements.TableComponent;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.events.basic.ResizeEvent;
import com.the.machine.framework.events.input.KeyDownEvent;
import com.the.machine.framework.events.input.KeyUpEvent;
import com.the.machine.framework.systems.canvas.CanvasElementSystem;
import com.the.machine.framework.systems.canvas.CanvasSystem;
import com.the.machine.framework.systems.canvas.TableCellSystem;
import com.the.machine.framework.systems.canvas.TableSystem;
import com.the.machine.framework.systems.physics.Light2dSystem;
import com.the.machine.framework.systems.physics.Physics2dSystem;
import com.the.machine.framework.systems.rendering.CameraRenderSystem;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.ClickEventListenerEventSpawner;
import com.the.machine.framework.utility.EntityUtilities;
import com.the.machine.framework.utility.Enums;
import com.the.machine.systems.DirectionalMovementSystem;
import com.the.machine.systems.InputControlledMovementSystem;
import com.the.machine.systems.MapSystem;

import java.util.HashMap;

import static com.badlogic.gdx.Input.Keys.*;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 04/03/15
 */
public class MapEditorSceneBuilder
		implements SceneBuilder {

	@Override
	public void createScene(World world) {
		world.addSystem(new DirectionalMovementSystem());
		world.addSystem(new InputControlledMovementSystem(), KeyDownEvent.class, KeyUpEvent.class);
		world.addSystem(new CameraRenderSystem(), AssetLoadingFinishedEvent.class);
		world.addSystem(new CanvasSystem(), ResizeEvent.class);
		world.addSystem(new CanvasElementSystem());
		world.addSystem(new TableSystem());
		world.addSystem(new TableCellSystem());
		world.addSystem(new MapSystem(), MapEditorSaveEvent.class, MapEditorLoadEvent.class);
		world.addSystem(new Physics2dSystem());
		world.addSystem(new Light2dSystem());

		Entity mapCamera = new Entity();
		CameraComponent mapCameraComponent = new CameraComponent();
		Bits mask = BitBuilder.all(32)
							  .c(0)
							  .get();
		mapCameraComponent.setCullingMask(mask);
		mapCameraComponent.setProjection(CameraComponent.Projection.ORTHOGRAPHIC);
		mapCameraComponent.getClippingPlanes()
						 .setNear(-300f);
		mapCamera.add(mapCameraComponent);
		TransformComponent transformComponent = new TransformComponent().setPosition(new Vector3(0, 0, 0));
		mapCamera.add(transformComponent);             // The transform component allows moving and rotating the mapCamera
		mapCamera.add(new NameComponent().setName("Map Editor Camera"));
		mapCamera.add(new DirectionalVelocityComponent());
		HashMap<Integer, InputControlledMovementSystem.Control> controlMap = new HashMap<>();
		controlMap.put(W, InputControlledMovementSystem.Control.RIGHT);
		controlMap.put(D, InputControlledMovementSystem.Control.UP);
		controlMap.put(S, InputControlledMovementSystem.Control.LEFT);
		controlMap.put(A, InputControlledMovementSystem.Control.DOWN);
		mapCamera.add(new ControlComponent().setControlMap(controlMap));
		world.addEntity(mapCamera);

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

		Entity map = new Entity();
		map.add(new TransformComponent().setZ(-1));
		map.add(new DimensionComponent().setDimension(300, 300));
		AreaComponent.AreaType type = AreaComponent.AreaType.GROUND;
		map.add(new AreaComponent().setType(type));
		map.add(new LayerComponent(BitBuilder.none(32)
											 .s(1)
											 .get()));
		map.add(new SpriteRenderComponent().setTextureRegion(type.getTextureAsset())
										   .setSortingLayer("Default"));
		world.addEntity(map);

		Entity GUITable = new Entity();
		GUITable.add(new CanvasElementComponent());
		GUITable.add(new TableComponent().setFillParent(true));
		GUITable.add(new TransformComponent());
		GUITable.add(new DimensionComponent());
		world.addEntity(GUITable);

		Entity saveButton = new Entity();
		CanvasElementComponent elementComponent = new CanvasElementComponent();
		elementComponent.getListeners()
						.add(new ClickEventListenerEventSpawner(world, MapEditorSaveEvent.class));
		saveButton.add(elementComponent);
		saveButton.add(new TableCellComponent().setHorizontalAlignment(Enums.HorizontalAlignment.LEFT).setVerticalAlignment(Enums.VerticalAlignment.TOP).setExpandY(1));
		saveButton.add(new ButtonComponent());
		saveButton.add(new TransformComponent());
		saveButton.add(new DimensionComponent().setDimension(100, 40).setOrigin(0,0));
		EntityUtilities.relate(GUITable, saveButton);
		world.addEntity(saveButton);

		Entity saveButtonLabel = EntityUtilities.makeLabel("Save Map").add(new TableCellComponent());
		EntityUtilities.relate(saveButton, saveButtonLabel);
		world.addEntity(saveButtonLabel);

		Entity loadButton = new Entity();
		CanvasElementComponent loadComponent = new CanvasElementComponent();
		loadComponent.getListeners()
						.add(new ClickEventListenerEventSpawner(world, MapEditorLoadEvent.class));
		loadButton.add(loadComponent);
		loadButton.add(new TableCellComponent().setExpandX(1).setHorizontalAlignment(Enums.HorizontalAlignment.LEFT).setVerticalAlignment(Enums.VerticalAlignment.TOP));
		loadButton.add(new ButtonComponent());
		loadButton.add(new TransformComponent().setPosition(110, 0, 0));
		loadButton.add(new DimensionComponent().setDimension(100, 40)
											   .setOrigin(0, 0));
		EntityUtilities.relate(GUITable, loadButton);
		world.addEntity(loadButton);

		Entity loadButtonLabel = EntityUtilities.makeLabel("Load Map")
												.add(new TableCellComponent());
		EntityUtilities.relate(loadButton, loadButtonLabel);
		world.addEntity(loadButtonLabel);

	}
}
