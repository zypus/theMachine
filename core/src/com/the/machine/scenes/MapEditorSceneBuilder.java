package com.the.machine.scenes;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.ControlComponent;
import com.the.machine.components.DirectionalVelocityComponent;
import com.the.machine.components.MapGroundComponent;
import com.the.machine.components.ResizableComponent;
import com.the.machine.components.SelectableComponent;
import com.the.machine.components.SelectionComponent;
import com.the.machine.components.SelectorComponent;
import com.the.machine.components.ZoomableComponent;
import com.the.machine.events.AudioEvent;
import com.the.machine.events.DoorCancelEvent;
import com.the.machine.events.DoorOpenEvent;
import com.the.machine.events.MapEditorHotbarEvent;
import com.the.machine.events.MapEditorLoadEvent;
import com.the.machine.events.MapEditorLoadPrefabEvent;
import com.the.machine.events.MapEditorSaveEvent;
import com.the.machine.events.MapEditorSavePrefabEvent;
import com.the.machine.events.MapEditorSaveSuccessEvent;
import com.the.machine.events.MarkerEvent;
import com.the.machine.events.ResetEvent;
import com.the.machine.events.SprintEvent;
import com.the.machine.events.TowerEnterEvent;
import com.the.machine.events.TowerLeaveEvent;
import com.the.machine.events.WindowDestroyEvent;
import com.the.machine.framework.SceneBuilder;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.CanvasComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.RemovalComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.ButtonComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.LabelComponent;
import com.the.machine.framework.components.canvasElements.SelectBoxComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.components.canvasElements.TableComponent;
import com.the.machine.framework.components.canvasElements.TextFieldComponent;
import com.the.machine.framework.components.physics.ColliderComponent;
import com.the.machine.framework.components.physics.Light2dComponent;
import com.the.machine.framework.components.physics.Physics2dComponent;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.events.basic.ResizeEvent;
import com.the.machine.framework.events.canvas.CanvasKeyboardFocusEvent;
import com.the.machine.framework.events.input.KeyDownEvent;
import com.the.machine.framework.events.input.KeyUpEvent;
import com.the.machine.framework.events.input.ScrolledEvent;
import com.the.machine.framework.events.input.TouchDownEvent;
import com.the.machine.framework.events.input.TouchDraggedEvent;
import com.the.machine.framework.events.input.TouchUpEvent;
import com.the.machine.framework.events.physics.ContactBeginEvent;
import com.the.machine.framework.events.physics.ContactEndEvent;
import com.the.machine.framework.events.physics.Light2dToggleEvent;
import com.the.machine.framework.systems.DelayedRemovalSystem;
import com.the.machine.framework.systems.RemovalSystem;
import com.the.machine.framework.systems.canvas.CanvasElementSystem;
import com.the.machine.framework.systems.canvas.CanvasSystem;
import com.the.machine.framework.systems.canvas.TableCellSystem;
import com.the.machine.framework.systems.canvas.TableSystem;
import com.the.machine.framework.systems.physics.Light2dSystem;
import com.the.machine.framework.systems.physics.Physics2dSystem;
import com.the.machine.framework.systems.rendering.CameraRenderSystem;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.BoolContainer;
import com.the.machine.framework.utility.ClickEventListenerEventSpawner;
import com.the.machine.framework.utility.ClickEventListenerExecuter;
import com.the.machine.framework.utility.EntityUtilities;
import com.the.machine.framework.utility.Enums;
import com.the.machine.framework.utility.Executable;
import com.the.machine.systems.AudioIndicatorSystem;
import com.the.machine.systems.AudioListeningSystem;
import com.the.machine.systems.BehaviourSystem;
import com.the.machine.systems.CameraZoomSystem;
import com.the.machine.systems.DirectionalMovementSystem;
import com.the.machine.systems.DiscretizedMapDebugSystem;
import com.the.machine.systems.DiscretizedMapSystem;
import com.the.machine.systems.DizzinessSystem;
import com.the.machine.systems.DoorSystem;
import com.the.machine.systems.DraggingSystem;
import com.the.machine.systems.EnvironmentSystem;
import com.the.machine.systems.GrowthSystem;
import com.the.machine.systems.InputControlledMovementSystem;
import com.the.machine.systems.IntruderSpawnSystem;
import com.the.machine.systems.MapSystem;
import com.the.machine.systems.MarkerSystem;
import com.the.machine.systems.MovementSystem;
import com.the.machine.systems.RandomNoiseSystem;
import com.the.machine.systems.ResizeHandleSystem;
import com.the.machine.systems.RotationSystem;
import com.the.machine.systems.SelectionSystem;
import com.the.machine.systems.SoundDirectionDebugSystem;
import com.the.machine.systems.SprintSystem;
import com.the.machine.systems.StepSoundSystem;
import com.the.machine.systems.TowerSystem;
import com.the.machine.systems.VictorySystem;
import com.the.machine.systems.VisionRangeDebugSystem;
import com.the.machine.systems.VisionSystem;
import com.the.machine.systems.VisionDebugSystem;
import com.the.machine.systems.WindowSystem;
import com.the.machine.systems.ZoomIndependenceSystem;

import java.util.HashMap;
import java.util.List;

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
		world.addSystem(new RemovalSystem());
		world.addSystem(new DelayedRemovalSystem());
		world.addSystem(new GrowthSystem());
		world.addSystem(new DirectionalMovementSystem());
		world.addSystem(new InputControlledMovementSystem(), KeyDownEvent.class, KeyUpEvent.class);
		world.addSystem(new CameraRenderSystem(), AssetLoadingFinishedEvent.class);
		world.addSystem(new TableSystem());
		world.addSystem(new TableCellSystem());
		world.addSystem(new CameraZoomSystem(), ScrolledEvent.class);
		world.addSystem(new ZoomIndependenceSystem());
		world.addSystem(new DraggingSystem(), TouchDownEvent.class, TouchDraggedEvent.class, TouchUpEvent.class);
		world.addSystem(new SelectionSystem(), TouchUpEvent.class);
		world.addSystem(new ResizeHandleSystem());
		world.addSystem(new CanvasSystem(), ResizeEvent.class, CanvasKeyboardFocusEvent.class);
		world.addSystem(new CanvasElementSystem());
		world.addSystem(new SprintSystem(), SprintEvent.class);
		world.addSystem(new IntruderSpawnSystem());
		world.addSystem(new MovementSystem());
		world.addSystem(new DizzinessSystem());
		world.addSystem(new EnvironmentSystem(), ContactBeginEvent.class, ContactEndEvent.class);
		world.addSystem(new RandomNoiseSystem());
		world.addSystem(new StepSoundSystem());
		world.addSystem(new AudioIndicatorSystem(), AudioEvent.class);
		world.addSystem(new AudioListeningSystem(), AudioEvent.class);
		world.addSystem(new SoundDirectionDebugSystem());
		world.addSystem(new DiscretizedMapSystem());
//		world.addSystem(new DiscretizedMapDebugSystem());
		world.addSystem(new VisionRangeDebugSystem());
		world.addSystem(new VisionSystem());
		world.addSystem(new VisionDebugSystem());
		world.addSystem(new RotationSystem());
		world.addSystem(new MarkerSystem(), MarkerEvent.class, ResetEvent.class);
		world.addSystem(new TowerSystem(), TowerEnterEvent.class, TowerLeaveEvent.class);
		world.addSystem(new DoorSystem(), DoorOpenEvent.class, DoorCancelEvent.class);
		world.addSystem(new WindowSystem(), WindowDestroyEvent.class);
		world.addSystem(new BehaviourSystem());
		world.addSystem(new VictorySystem());
		world.addSystem(new Physics2dSystem());
		world.addSystem(new MapSystem(), MapEditorSaveEvent.class, MapEditorLoadEvent.class, MapEditorHotbarEvent.class, TouchUpEvent.class, KeyDownEvent.class, MapEditorSavePrefabEvent.class, MapEditorLoadPrefabEvent.class);
		world.addSystem(new Light2dSystem(), Light2dToggleEvent.class);

		toggleSimulationSystems(world, false);

		Entity mapCamera = new Entity();
		CameraComponent mapCameraComponent = new CameraComponent();
		Bits mask = BitBuilder.all(32)
							  .c(0)
							  .get();
		mapCameraComponent.setCullingMask(mask);
		mapCameraComponent.setProjection(CameraComponent.Projection.ORTHOGRAPHIC);
		mapCameraComponent.setZoom(0.3f);
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
		map.add(new DimensionComponent().setDimension(200, 200));
		AreaComponent.AreaType type = AreaComponent.AreaType.GROUND;
		map.add(new AreaComponent().setType(type));
		map.add(new LayerComponent(BitBuilder.none(32)
											 .s(1)
											 .get()));
		map.add(new SpriteRenderComponent().setTextureRegion(type.getTextureAsset())
										   .setSortingLayer("Default"));
		map.add(new SelectableComponent());
		map.add(new ResizableComponent());
		map.add(new Physics2dComponent().setType(BodyDef.BodyType.StaticBody));
		map.add(new ColliderComponent()
						.add(new ColliderComponent.Collider().setShape(new Vector2(-100, -100), new Vector2(100, -100)))
						.add(new ColliderComponent.Collider().setShape(new Vector2(100, -100), new Vector2(100, 100)))
						.add(new ColliderComponent.Collider().setShape(new Vector2(100, 100), new Vector2(-100, 100)))
						.add(new ColliderComponent.Collider().setShape(new Vector2(-100, 100), new Vector2(-100, -100))));
		map.add(new MapGroundComponent());
		world.addEntity(map);

		Entity GUITable = new Entity();
		GUITable.add(new CanvasElementComponent().setTouchable(Touchable.childrenOnly));
		GUITable.add(new TableComponent().setFillParent(true));
		GUITable.add(new TransformComponent());
		GUITable.add(new DimensionComponent());
		world.addEntity(GUITable);

		// loading and saving of whole maps
		int spacing = 5;
		// saving
		{
			TextFieldComponent textFieldComponent = new TextFieldComponent().setMaxLength(20);
			Entity mapName = EntityUtilities.makeTextField(textFieldComponent);
			mapName.add(new TableCellComponent().setHorizontalAlignment(Enums.HorizontalAlignment.LEFT)
												.setVerticalAlignment(Enums.VerticalAlignment.TOP)
			.setSpace(new Value.Fixed(spacing)));

			SelectBoxComponent selectBoxComponent = new SelectBoxComponent();
			Entity loadName = EntityUtilities.makeSelectBox(selectBoxComponent);
			loadName.add(new TableCellComponent()
								 .setHorizontalAlignment(Enums.HorizontalAlignment.LEFT)
								 .setVerticalAlignment(Enums.VerticalAlignment.TOP)
								 .setSpace(new Value.Fixed(spacing)));
			Asset.SmartFileHandleResolver resolver = new Asset.SmartFileHandleResolver();
			Executable loadMapNames = () -> {
				List<FileHandle> prefabs = resolver.findFiles(Gdx.files.local("prefabs"), ".map.prefab");
				Array<String> names = new Array<>(prefabs.size());
				for (FileHandle prefab : prefabs) {
					names.add(prefab.nameWithoutExtension()
									.split("\\.")[0]);
				}
				selectBoxComponent.setItems(names);
				return true;
			};
			loadMapNames.execute();
			EventListener saveSuccessListener = (e) -> {
				loadMapNames.execute();
			};
			world.register(saveSuccessListener, MapEditorSaveSuccessEvent.class);

			Entity saveButton = new Entity();
			CanvasElementComponent elementComponent = new CanvasElementComponent();
			elementComponent.getListeners()
							.add(new ClickEventListenerExecuter(() -> {
								String name = textFieldComponent.getTextField()
																.getText();
								if (name.equals("")) {
									name = "Default";
									textFieldComponent.setText(name);
								}
								world.dispatchEvent(new MapEditorSaveEvent(name));
								world.dispatchEvent(new CanvasKeyboardFocusEvent(null));
								return true;
							}));
			saveButton.add(elementComponent);
			saveButton.add(new TableCellComponent().setHorizontalAlignment(Enums.HorizontalAlignment.LEFT)
												   .setVerticalAlignment(Enums.VerticalAlignment.TOP)
												   .setSpace(new Value.Fixed(spacing)));
			saveButton.add(new ButtonComponent());
			saveButton.add(new TransformComponent());
			saveButton.add(new DimensionComponent().setDimension(100, 40)
												   .setOrigin(0, 0));
			EntityUtilities.relate(GUITable, saveButton);
			world.addEntity(saveButton);

			Entity saveButtonLabel = EntityUtilities.makeLabel("Save Map ->")
													.add(new TableCellComponent());
			EntityUtilities.relate(saveButton, saveButtonLabel);
			world.addEntity(saveButtonLabel);

			EntityUtilities.relate(GUITable, mapName);
			world.addEntity(mapName);

			// loading

			Entity loadButton = new Entity();
			CanvasElementComponent loadComponent = new CanvasElementComponent();
			//		loadComponent.getListeners()
			//					 .add(new ClickEventListenerEventSpawner(world, new MapEditorLoadEvent()));
			// Experiment for a loading dialog
			loadComponent.getListeners()
						 .add(new ClickEventListenerExecuter(() -> {
							 String name = (String) selectBoxComponent.getSelectBox()
																	  .getSelection()
																	  .first();
							 textFieldComponent.getTextField()
											   .setText(name);
							 world.dispatchEvent(new MapEditorLoadEvent(name));
							 return true;
						 }));
			loadButton.add(loadComponent);
			loadButton.add(new TableCellComponent()
								   .setHorizontalAlignment(Enums.HorizontalAlignment.LEFT)
								   .setVerticalAlignment(Enums.VerticalAlignment.TOP)
								   .setSpace(new Value.Fixed(spacing)));
			loadButton.add(new ButtonComponent());
			loadButton.add(new TransformComponent().setPosition(110, 0, 0));
			loadButton.add(new DimensionComponent().setDimension(100, 40)
												   .setOrigin(0, 0));
			EntityUtilities.relate(GUITable, loadButton);
			world.addEntity(loadButton);

			EntityUtilities.relate(GUITable, loadName);
			world.addEntity(loadName);

			Entity loadButtonLabel = EntityUtilities.makeLabel("Load Map ->")
													.add(new TableCellComponent());
			EntityUtilities.relate(loadButton, loadButtonLabel);
			world.addEntity(loadButtonLabel);
		}

		/**========================================================================================**/

		// play / pause / reset button
		{
			LabelComponent playLabelComponent = new LabelComponent().setText("Play");
			Entity playButtonLabel = EntityUtilities.makeLabel(playLabelComponent)
													.add(new TableCellComponent());

			Entity playButton = new Entity();
			CanvasElementComponent playComponent = new CanvasElementComponent();
			//		resetComponent.getListeners()
			//					 .add(new ClickEventListenerEventSpawner(world, new MapEditorLoadEvent()));
			// Experiment for a loading dialog
			final BoolContainer resetted = new BoolContainer();
			resetted.setBool(true);
			playComponent.getListeners()
						 .add(new ClickEventListenerExecuter(() -> {
							 String text = playLabelComponent.getText();
							 if (text.equals("Play")) {
								 if (resetted.isBool()) {
									 ImmutableArray<Entity> mapElements = world.getEntitiesFor(Family.one(AreaComponent.class, Light2dComponent.class)
																									 .get());
									 Entity[] array = new Entity[mapElements.size()];
									 int index = 0;
									 for (Entity element : mapElements) {
										 element.remove(SelectionComponent.class);
										 array[index] = element;
										 index++;
									 }
									 world.savePrefab("active_map", array);
									 resetted.setBool(false);
								 }
								 toggleSimulationSystems(world, true);
								 playLabelComponent.setText("Pause");
							 } else {
								 toggleSimulationSystems(world, false);
								 playLabelComponent.setText("Play");
							 }
							 playLabelComponent.setDirty(true);
							 return true;
						 }));
			playButton.add(playComponent);
			playButton.add(new TableCellComponent()
								   .setHorizontalAlignment(Enums.HorizontalAlignment.RIGHT)
								   .setVerticalAlignment(Enums.VerticalAlignment.TOP)
								   .setExpandX(1)
								   .setSpace(new Value.Fixed(spacing)));
			playButton.add(new ButtonComponent());
			playButton.add(new TransformComponent().setPosition(110, 0, 0));
			playButton.add(new DimensionComponent().setDimension(100, 40)
												   .setOrigin(0, 0));
			EntityUtilities.relate(GUITable, playButton);
			world.addEntity(playButton);

			EntityUtilities.relate(playButton, playButtonLabel);
			world.addEntity(playButtonLabel);

			// reset
			LabelComponent resetLabelComponent = new LabelComponent().setText("Reset");
			Entity resetButtonLabel = EntityUtilities.makeLabel(resetLabelComponent)
													.add(new TableCellComponent());

			Entity resetButton = new Entity();
			CanvasElementComponent resetComponent = new CanvasElementComponent();
			resetted.setBool(true);
			resetComponent.getListeners()
						 .add(new ClickEventListenerExecuter(() -> {
							 playLabelComponent.setText("Play");
							 playLabelComponent.setDirty(true);
							 toggleSimulationSystems(world, false);
							 ImmutableArray<Entity> mapElements = world.getEntitiesFor(Family.one(AreaComponent.class, Light2dComponent.class)
																							 .get());
							 Entity[] array = new Entity[mapElements.size()];
							 int index = 0;
							 for (Entity element : mapElements) {
								 element.remove(SelectionComponent.class);
								 array[index] = element;
								 index++;
							 }
							 Entity[] newEntities = world.loadPrefab("active_map");
							 if (newEntities != null && newEntities.length > 0) {
								 for (Entity entity : array) {
									 entity.add(new RemovalComponent());
								 }
							 }
							 resetted.setBool(true);
							 world.dispatchEvent(new ResetEvent());
							 return true;
						 }));
			resetButton.add(resetComponent);
			resetButton.add(new TableCellComponent()
									.setHorizontalAlignment(Enums.HorizontalAlignment.LEFT)
									.setVerticalAlignment(Enums.VerticalAlignment.TOP)
									.setExpandX(1)
									.setSpace(new Value.Fixed(spacing)));
			resetButton.add(new ButtonComponent());
			resetButton.add(new TransformComponent().setPosition(110, 0, 0));
			resetButton.add(new DimensionComponent().setDimension(100, 40)
												   .setOrigin(0, 0));
			EntityUtilities.relate(GUITable, resetButton);
			world.addEntity(resetButton);

			EntityUtilities.relate(resetButton, resetButtonLabel);
			world.addEntity(resetButtonLabel);

		}

		/**========================================================================================**/

		// loading and saving of partial map elements
		{
			// saving
			TextFieldComponent textFieldComponent = new TextFieldComponent().setMaxLength(20);
			Entity mapName = EntityUtilities.makeTextField(textFieldComponent);
			mapName.add(new TableCellComponent().setHorizontalAlignment(Enums.HorizontalAlignment.RIGHT)
												.setVerticalAlignment(Enums.VerticalAlignment.TOP)
												.setSpace(new Value.Fixed(spacing)));

			SelectBoxComponent selectBoxComponent = new SelectBoxComponent();
			Entity loadName = EntityUtilities.makeSelectBox(selectBoxComponent);
			loadName.add(new TableCellComponent()
								 .setHorizontalAlignment(Enums.HorizontalAlignment.RIGHT)
								 .setVerticalAlignment(Enums.VerticalAlignment.TOP)
								 .setSpace(new Value.Fixed(spacing))
								 .setRowEnd(true));
			Asset.SmartFileHandleResolver resolver = new Asset.SmartFileHandleResolver();
			Executable loadMapNames = () -> {
				List<FileHandle> prefabs = resolver.findFiles(Gdx.files.local("prefabs"), ".obj.prefab");
				Array<String> names = new Array<>(prefabs.size());
				for (FileHandle prefab : prefabs) {
					names.add(prefab.nameWithoutExtension()
									.split("\\.")[0]);
				}
				selectBoxComponent.setItems(names);
				return true;
			};
			loadMapNames.execute();
			EventListener saveSuccessListener = (e) -> {
				loadMapNames.execute();
			};
			world.register(saveSuccessListener, MapEditorSaveSuccessEvent.class);

			Entity saveButton = new Entity();
			CanvasElementComponent elementComponent = new CanvasElementComponent();
			elementComponent.getListeners()
							.add(new ClickEventListenerExecuter(() -> {
								String name = textFieldComponent.getTextField()
																.getText();
								if (name.equals("")) {
									name = "Default";
									textFieldComponent.setText(name);
								}
								world.dispatchEvent(new MapEditorSavePrefabEvent(name));
								world.dispatchEvent(new CanvasKeyboardFocusEvent(null));
								return true;
							}));
			saveButton.add(elementComponent);
			saveButton.add(new TableCellComponent().setHorizontalAlignment(Enums.HorizontalAlignment.RIGHT)
												   .setVerticalAlignment(Enums.VerticalAlignment.TOP)
												   .setSpace(new Value.Fixed(spacing)));
			saveButton.add(new ButtonComponent());
			saveButton.add(new TransformComponent());
			saveButton.add(new DimensionComponent().setDimension(100, 40)
												   .setOrigin(0, 0));
			EntityUtilities.relate(GUITable, saveButton);
			world.addEntity(saveButton);

			Entity saveButtonLabel = EntityUtilities.makeLabel("Save Prefab ->")
													.add(new TableCellComponent());
			EntityUtilities.relate(saveButton, saveButtonLabel);
			world.addEntity(saveButtonLabel);

			EntityUtilities.relate(GUITable, mapName);
			world.addEntity(mapName);

			// loading

			Entity loadButton = new Entity();
			CanvasElementComponent loadComponent = new CanvasElementComponent();
			//		loadComponent.getListeners()
			//					 .add(new ClickEventListenerEventSpawner(world, new MapEditorLoadEvent()));
			// Experiment for a loading dialog
			loadComponent.getListeners()
						 .add(new ClickEventListenerExecuter(() -> {
							 String name = (String) selectBoxComponent.getSelectBox()
																	  .getSelection()
																	  .first();
							 textFieldComponent.getTextField()
											   .setText(name);
							 world.dispatchEvent(new MapEditorLoadPrefabEvent(name));
							 return true;
						 }));
			loadButton.add(loadComponent);
			loadButton.add(new TableCellComponent()
								   .setHorizontalAlignment(Enums.HorizontalAlignment.RIGHT)
								   .setVerticalAlignment(Enums.VerticalAlignment.TOP)
								   .setSpace(new Value.Fixed(spacing)));
			loadButton.add(new ButtonComponent());
			loadButton.add(new TransformComponent().setPosition(110, 0, 0));
			loadButton.add(new DimensionComponent().setDimension(100, 40)
												   .setOrigin(0, 0));
			EntityUtilities.relate(GUITable, loadButton);
			world.addEntity(loadButton);

			EntityUtilities.relate(GUITable, loadName);
			world.addEntity(loadName);

			Entity loadButtonLabel = EntityUtilities.makeLabel("Load Prefab ->")
													.add(new TableCellComponent());
			EntityUtilities.relate(loadButton, loadButtonLabel);
			world.addEntity(loadButtonLabel);
		}
		// some special options
		Entity lightSwitch = new Entity();
		CanvasElementComponent lcec = new CanvasElementComponent().setStyleValue("toggle");
		lcec.getListeners()
		   .add(new ClickEventListenerEventSpawner(world, new Light2dToggleEvent()));
		lightSwitch.add(lcec);
		TableCellComponent ltcc = new TableCellComponent().setVerticalAlignment(Enums.VerticalAlignment.CENTER)
														 .setHorizontalAlignment(Enums.HorizontalAlignment.RIGHT)
														 .setPrefWidth(new Value.Fixed(50))
														 .setPrefHeight(new Value.Fixed(50))
														 .setSpace(new Value.Fixed(spacing))
				.setColspan(10).setExpandY(1)
														 .setExpandX(1)
				.setRowEnd(true);
		lightSwitch.add(ltcc);
		lightSwitch.add(new ButtonComponent());
		lightSwitch.add(new TransformComponent());
		lightSwitch.add(new DimensionComponent().setDimension(100, 100)
												.setOrigin(0, 0));
		EntityUtilities.relate(GUITable, lightSwitch);
		world.addEntity(lightSwitch);
		Entity lightLabel = EntityUtilities.makeLabel("Light")
										 .add(new TableCellComponent());
		EntityUtilities.relate(lightSwitch, lightLabel);
		world.addEntity(lightLabel);

		// make hot bar buttons for switching between the different area types
		Entity hotbar = new Entity();
		hotbar.add(new TableCellComponent().setExpandX(1)
										   .setFillX(1)
										   .setColspan(10));
		hotbar.add(new TableComponent());
		hotbar.add(new CanvasElementComponent());
		hotbar.add(new TransformComponent());
		hotbar.add(new DimensionComponent());
		EntityUtilities.relate(GUITable, hotbar);
		world.addEntity(hotbar);

		String[] buttonNames = new String[]{"Wall", "Window(T)", "Door(T)", "Cover", "Tower", "Target", "Agent(T)", "Prefab"};
		ButtonGroup<Button> buttonGroup = new ButtonGroup<>();
		buttonGroup.setMinCheckCount(1);
		buttonGroup.setMaxCheckCount(1);
		for (int i = 0; i < buttonNames.length; i++) {
			Entity hbb = new Entity();
			CanvasElementComponent cec = new CanvasElementComponent().setStyleValue("toggle");
			cec.getListeners()
							.add(new ClickEventListenerEventSpawner(world, new MapEditorHotbarEvent(i)));
			hbb.add(cec);
			TableCellComponent tcc = new TableCellComponent().setVerticalAlignment(Enums.VerticalAlignment.BOTTOM).setPrefWidth(new Value.Fixed(50)).setPrefHeight(new Value.Fixed(50))
															 .setSpace(new Value.Fixed(spacing));
			if (i == buttonNames.length -1) {
				tcc.setRowEnd(true);
			}
//			if (i == 0) {
//				tcc.setExpandX(2);
//				tcc.setHorizontalAlignment(Enums.HorizontalAlignment.RIGHT);
//			}
			hbb.add(tcc);
			ButtonComponent buttonComponent = new ButtonComponent().setButtonGroup(buttonGroup);
			hbb.add(buttonComponent);
			hbb.add(new TransformComponent());
			hbb.add(new DimensionComponent().setDimension(100, 100)
												   .setOrigin(0, 0));
			EntityUtilities.relate(hotbar, hbb);
			world.addEntity(hbb);
			String text = buttonNames[i];
			Entity hbbLabel = EntityUtilities.makeLabel((i+1)+" - "+text)
													.add(new TableCellComponent());
			EntityUtilities.relate(hbb, hbbLabel);
			world.addEntity(hbbLabel);

			final int index = i;
			EventListener hotbarListener = (e) -> {
				MapEditorHotbarEvent hotbarEvent = (MapEditorHotbarEvent) e;
				int hi = hotbarEvent.getHotbarIndex();
				if (hi == index) {
					buttonComponent.getButton().setChecked(true);
				}
			};
			world.register(hotbarListener, MapEditorHotbarEvent.class);
		}

	}

	public static void toggleSimulationSystems(World world, boolean enabled) {
		world.setSystemStatus(DelayedRemovalSystem.class, enabled);
		world.setSystemStatus(GrowthSystem.class, enabled);
		world.setSystemStatus(MovementSystem.class, enabled);
		world.setSystemStatus(RandomNoiseSystem.class, enabled);
		world.setSystemStatus(RotationSystem.class, enabled);
		world.setSystemStatus(BehaviourSystem.class, enabled);
//		world.setSystemStatus(RandomBehaviourSystem.class, enabled);
		world.setSystemStatus(DiscretizedMapSystem.class, enabled);
		world.setSystemStatus(DiscretizedMapDebugSystem.class, enabled);
		world.setSystemStatus(VisionSystem.class, enabled);
		world.setSystemStatus(StepSoundSystem.class, enabled);
		world.setSystemStatus(DizzinessSystem.class, enabled);
		world.setSystemStatus(SprintSystem.class, enabled);
		world.setSystemStatus(IntruderSpawnSystem.class, enabled);
		world.setSystemStatus(VisionDebugSystem.class, enabled);
		world.setSystemStatus(WindowSystem.class, enabled);
		world.setSystemStatus(DoorSystem.class, enabled);
		world.setSystemStatus(TowerSystem.class, enabled);
		world.setSystemStatus(MarkerSystem.class, enabled);
		world.setSystemStatus(VictorySystem.class, enabled);
//		world.setSystemStatus(WorldMappingSystem.class, enabled);
//		world.setSystemStatus(AgentSightSystem.class, enabled);
	}

	private static void toggleMapEditorSystems(World world, boolean enabled) {

	}
}
