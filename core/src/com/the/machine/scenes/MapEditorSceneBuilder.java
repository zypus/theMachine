package com.the.machine.scenes;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Bits;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.ControlComponent;
import com.the.machine.components.DirectionalVelocityComponent;
import com.the.machine.components.DraggableComponent;
import com.the.machine.components.ResizableComponent;
import com.the.machine.components.SelectableComponent;
import com.the.machine.components.SelectorComponent;
import com.the.machine.components.ZoomableComponent;
import com.the.machine.events.MapEditorHotbarEvent;
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
import com.the.machine.framework.events.input.ScrolledEvent;
import com.the.machine.framework.events.input.TouchDownEvent;
import com.the.machine.framework.events.input.TouchDraggedEvent;
import com.the.machine.framework.events.input.TouchUpEvent;
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
import com.the.machine.systems.CameraZoomSystem;
import com.the.machine.systems.DirectionalMovementSystem;
import com.the.machine.systems.DraggingSystem;
import com.the.machine.systems.InputControlledMovementSystem;
import com.the.machine.systems.MapSystem;
import com.the.machine.systems.ResizeHandleSystem;
import com.the.machine.systems.SelectionSystem;

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
		world.addSystem(new TableSystem());
		world.addSystem(new TableCellSystem());
		world.addSystem(new CameraZoomSystem(), ScrolledEvent.class);
		world.addSystem(new DraggingSystem(), TouchDownEvent.class, TouchDraggedEvent.class, TouchUpEvent.class);
		world.addSystem(new SelectionSystem(), TouchUpEvent.class);
		world.addSystem(new ResizeHandleSystem());
		world.addSystem(new CanvasSystem(), ResizeEvent.class);
		world.addSystem(new CanvasElementSystem());
		world.addSystem(new Physics2dSystem());
		world.addSystem(new MapSystem(), MapEditorSaveEvent.class, MapEditorLoadEvent.class, MapEditorHotbarEvent.class, TouchUpEvent.class, KeyDownEvent.class);
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
		mapCamera.add(new SelectorComponent());
		mapCamera.add(new ZoomableComponent());
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
		uiCamera.add(new SelectorComponent());
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
		map.add(new DraggableComponent());
		map.add(new SelectableComponent());
		map.add(new ResizableComponent());
		world.addEntity(map);

		Entity GUITable = new Entity();
		GUITable.add(new CanvasElementComponent().setTouchable(Touchable.childrenOnly));
		GUITable.add(new TableComponent().setFillParent(true));
		GUITable.add(new TransformComponent());
		GUITable.add(new DimensionComponent());
		world.addEntity(GUITable);

		Entity saveButton = new Entity();
		CanvasElementComponent elementComponent = new CanvasElementComponent();
		elementComponent.getListeners()
						.add(new ClickEventListenerEventSpawner(world, new MapEditorSaveEvent()));
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
					 .add(new ClickEventListenerEventSpawner(world, new MapEditorLoadEvent()));
		// Experiment for a loading dialog
/*		loadComponent.getListeners()
						.add(new ClickEventListenerExecuter(() -> {
							Entity loadDialog = new Entity();
							loadDialog.add(new CanvasElementComponent());
							loadDialog.add(new TableComponent().setFillParent(true));
							loadDialog.add(new TransformComponent());
							loadDialog.add(new DimensionComponent());
							EntityUtilities.relate(canvas, loadDialog);
							world.addEntity(loadDialog);

							CanvasElementComponent cancel = new CanvasElementComponent();
							cancel.getListeners().add(new ClickEventListenerExecuter(() -> {
								world.removeEntity(loadDialog);
								return true;
							}));
							Entity cancelButton = EntityUtilities.makeButton(cancel);
							cancelButton.add(new TableCellComponent());
							Entity cancelLabel = EntityUtilities.makeLabel("Cancel").add(new TableCellComponent());
							EntityUtilities.relate(cancelButton, cancelLabel);
							world.addEntity(cancelLabel);
							EntityUtilities.relate(loadDialog, cancelButton);
							world.addEntity(cancelButton, true);
							return true;
						}));*/
		loadButton.add(loadComponent);
		loadButton.add(new TableCellComponent().setExpandX(1)
											   .setHorizontalAlignment(Enums.HorizontalAlignment.LEFT)
											   .setVerticalAlignment(Enums.VerticalAlignment.TOP)
											   .setColspan(5)
											   .setRowEnd(true));
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

		// make hot bar buttons for switching between the different area types
		int hotbarCount = 7;
		for (int i = 0; i < hotbarCount; i++) {
			Entity hbb = new Entity();
			CanvasElementComponent cec = new CanvasElementComponent();
			cec.getListeners()
							.add(new ClickEventListenerEventSpawner(world, new MapEditorHotbarEvent(i)));
			hbb.add(cec);
			TableCellComponent tcc = new TableCellComponent().setVerticalAlignment(Enums.VerticalAlignment.BOTTOM).setPrefWidth(new Value.Fixed(50)).setPrefHeight(new Value.Fixed(50));
			if (i == hotbarCount-1) {
				tcc.setRowEnd(true);
			}
			hbb.add(tcc);
			hbb.add(new ButtonComponent());
			hbb.add(new TransformComponent());
			hbb.add(new DimensionComponent().setDimension(100, 100)
												   .setOrigin(0, 0));
			EntityUtilities.relate(GUITable, hbb);
			world.addEntity(hbb);
			String text = (i == 0) ? "Wall" : (i == 1) ? "Window(T)" : (i == 2) ? "Door(T)" : (i == 3) ? "Cover" : (i == 4) ? "Tower(T)" : (i == 6) ? "Target" : "Agent";
			Entity hbbLabel = EntityUtilities.makeLabel(text)
													.add(new TableCellComponent());
			EntityUtilities.relate(hbb, hbbLabel);
			world.addEntity(hbbLabel);
		}

	}
}
