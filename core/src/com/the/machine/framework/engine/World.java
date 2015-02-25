package com.the.machine.framework.engine;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.SceneBuilder;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.assets.LoadingTexture;
import com.the.machine.framework.components.DisabledComponent;
import com.the.machine.framework.components.ObservableComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.WorldComponent;
import com.the.machine.framework.container.Scene;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventEngine;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.events.basic.ResizeEvent;
import com.the.machine.framework.serialization.EntitySerializer;
import com.the.machine.framework.utility.EntityUtilities;
import lombok.Getter;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created $(DATE)
 */
public class World implements ApplicationListener {

	private EventEngine  eventEngine;
	private Engine       engine;
	private TweenManager tweenManager;

	WeakReference<Entity> worldEntityRef;

	Kryo kryo = new Kryo();

	private Rectangle worldBounds;

	private WorldState worldState;

	private byte[] loadedData;
	private Scene activeScene;
	private byte[] activeData;

	@Getter private ImmutableArray<Bits> layers;
	@Getter private Map<String, Integer> sortingLayers = new HashMap<>();

	private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	private ComponentMapper<ParentComponent>    parents    = ComponentMapper.getFor(ParentComponent.class);
	private ComponentMapper<SubEntityComponent> subs       = ComponentMapper.getFor(SubEntityComponent.class);
	private ComponentMapper<DisabledComponent>  disableds  = ComponentMapper.getFor(DisabledComponent.class);
	private ComponentMapper<WorldComponent> worlds = ComponentMapper.getFor(WorldComponent.class);

	@Getter private CameraGroupStrategy cameraGroupStrategy;
	@Getter private DecalBatch          decalBatch;

	@Getter private float t        = 0;
	private         float timeFlow = 1f;

	private boolean stableOnly = false;

	private boolean loadingAssets = false;

	public World() {
		this(null);
	}

	public World(Rectangle bounds) {
		worldState = WorldState.INITIALIZED;
		worldBounds = bounds;
		eventEngine = new EventEngine();
		engine = new Engine();
		tweenManager = new TweenManager();
		kryo.addDefaultSerializer(Entity.class, new EntitySerializer());
	}

	public FileHandle locateScene(String name) {
		FileHandle internal = Gdx.files.local("");
		FileHandle[] list = internal.list("scenes");
		FileHandle scene;
		if (list.length > 0 && list[0].isDirectory()) {
			FileHandle sceneDir = list[0];
			scene = findFile(sceneDir, name);
		} else {
			scene = findFile(internal, name);
		}
		return scene;
	}

	public void loadScene(String name) {
		FileHandle scene = locateScene(name);
		if (scene != null) {
			loadedData = scene.readBytes();
			setActiveScene(loadedData);
		} else {
			System.out.println("Scene '" + name + "' not found.");
		}
	}

	public void reload() {
		setActiveScene(loadedData);
	}

	public void setActiveScene(byte[] data) {
		activeData = data;
		resetActiveScene();
	}

	public void resetActiveScene() {
		loadActiveScene();
	}

	private void loadActiveScene() {
		InputStream inputStream = new ByteArrayInputStream(activeData);
		Input input = new Input(inputStream);
		activeScene = kryo.readObject(input, Scene.class);
		input.close();
		layers = activeScene.getLayers();
		for (AbstractSystem system : activeScene.getSystems()) {
			if (system.getEvents() != null) {
				addSystem(system, system.getEvents());
			} else {
				addSystem(system);
			}
		}
		for (Entity entity : activeScene.getEntities()) {
			addEntity(entity);
			if (worlds.has(entity)) {
				Entity previousWorld = worldEntityRef.get();
				WorldComponent worldComponent = worlds.get(entity);
				worldComponent.setWorld(worlds.get(previousWorld).getWorld());
				removeEntity(previousWorld);
				worldEntityRef = new WeakReference<>(entity);
			}
		}
		worldState = WorldState.RUNNING;
	}

	private FileHandle newSceneLocation(String name) {
		FileHandle internal = Gdx.files.local("");
		FileHandle[] list = internal.list("scenes");
		if (list.length == 0) {
			FileHandle scenes = Gdx.files.local("scenes");
			scenes.mkdirs();
		}
		return Gdx.files.local("scenes/" + name);
	}

	public void saveActiveScene() {
		FileHandle scene = locateScene(activeScene.getName());
		if (scene == null) {
			scene = newSceneLocation(activeScene.getName());
		}
		scene.writeBytes(activeData, false);
	}

	public void updateActiveScene() {
		WorldState previousState = worldState;
		worldState = WorldState.FULL_PAUSED;
		ImmutableArray<EntitySystem> entitySystems = engine.getSystems();
		Array<AbstractSystem> systems = new Array<>(entitySystems.size());
		for (EntitySystem system : entitySystems) {
			systems.add((AbstractSystem) system);
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Output output = new Output(outputStream);
		if (activeScene == null) {
			activeScene = new Scene();
			activeScene.setName("Unnamed Scene");
		}
		activeScene.setSystems(new ImmutableArray<>(systems));
		activeScene.setEntities(engine.getEntities());
		activeScene.setLayers(layers);
		kryo.writeObject(output, activeScene);
		output.close();
		activeData = outputStream.toByteArray();
		worldState = previousState;
	}

	private FileHandle findFile(FileHandle origin, String name) {
		if (origin.name()
				  .equals(name)) {
			return origin;
		} else {
			for (FileHandle child : origin.list()) {
				FileHandle result = findFile(child, name);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public void setup() {
		Entity worldEntity = worldEntityRef.get();
		removeAllSystems();
		removeAllEntities();
		addEntity(worldEntity);
	}

	public void buildScene(SceneBuilder builder) {
		WorldState previousState = worldState;
		worldState = WorldState.FULL_PAUSED;
		setup();
		builder.createScene(this);
		worldState = previousState;
		updateActiveScene();
		saveActiveScene();

		resetActiveScene();
	}

	@Override
	public void create() {
		if (worldBounds == null) {
			new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}

		cameraGroupStrategy = new CameraGroupStrategy(new OrthographicCamera());
		decalBatch = new DecalBatch(cameraGroupStrategy);

		Entity worldEntity = new Entity();
		worldEntity.add(new WorldComponent().setWorld(this));
		worldEntityRef = new WeakReference<>(worldEntity);
		addEntity(worldEntity);

//		loadScene("Unnamed Scene");

		// systems
//		addSystem(new CameraRenderSystem(), AssetLoadingFinishedEvent.class);
//		addSystem(new CanvasSystem(), ResizeEvent.class);
//		addSystem(new CanvasElementSystem());
//		addSystem(new TableSystem(), ResizeEvent.class);
//		addSystem(new TableCellSystem());
//		addSystem(new TreeSystem());
//		addSystem(new TreeNodeSystem());
//		addSystem(new WorldTreeSystem());
//
//		// entities
//		Entity camera = new Entity();
//		CameraComponent cameraComponent = new CameraComponent();
//		Bits mask = BitBuilder.all(32)
//							  .c(0)
//							  .get();
//		cameraComponent.setCullingMask(mask);
//		cameraComponent.setProjection(CameraComponent.Projection.PERSPECTIVE);
//		cameraComponent.setZoom(0.01f);
//		camera.add(cameraComponent);
//		TransformComponent transformComponent = new TransformComponent().setPosition(new Vector3(0, 0, 5));
//		camera.add(transformComponent);
//		camera.add(new NameComponent().setName("Main Camera"));
//		addEntity(camera);
//
//		Entity mapCamera = new Entity();
//		mapCamera.add(new CameraComponent()
//							  .setCullingMask(BitBuilder.all(32)
//														.c(0)
//														.get())
//							  .setProjection(CameraComponent.Projection.ORTHOGRAPHIC)
//							  .setZoom(0.05f)
//							  .setViewportRect(new Rectangle(0.8f, 0.8f, 0.2f, 0.2f))
//							  .setDepth(10)
//							  .setBackground(new Color(0, 0.3f, 0.3f, 1)));
//		mapCamera.add(new NameComponent().setName("Map Camera"));
//		addEntity(mapCamera);
//
//		Entity uiCamera = new Entity();
//		CameraComponent uiCameraComponent = new CameraComponent();
//		uiCameraComponent.setCullingMask(BitBuilder.none(32)
//												   .s(0)
//												   .get());
//		uiCameraComponent.setProjection(CameraComponent.Projection.ORTHOGRAPHIC);
//		uiCameraComponent.setZoom(1f);
//		uiCameraComponent.setDepth(100);
//		uiCameraComponent.setClearFlag(0);
//		uiCameraComponent.setOrigin(new Vector2(0f, 0f));
//		uiCameraComponent.getClippingPlanes().setNear(-300f);
//		uiCamera.add(uiCameraComponent);
//		uiCamera.add(new TransformComponent().setPosition(0,0,0));
//		uiCamera.add(new NameComponent().setName("UI Camera"));
//		addEntity(uiCamera);
//
//		Asset<TextureRegion> textureRegion = Asset.fetch("badlogic", TextureRegion.class);
//
//		Entity test = new Entity();
//		SpriteRenderComponent spriteRenderComponent = new SpriteRenderComponent().setTextureRegion(textureRegion).setSortingLayer("Default");
//		test.add(new LayerComponent(BitBuilder.none(32).s(1).get()));
//		test.add(spriteRenderComponent);
//		TransformComponent spriteTransform = new TransformComponent().setPosition(new Vector3(0, 0, 0))
//																	 .setZRotation(0)
//																	 .setScale(1f);
//		test.add(spriteTransform);
//		SubEntityComponent subEntityComponent = new SubEntityComponent();
//		test.add(subEntityComponent);
//		test.add(new NameComponent().setName("Badlogic"));
//		addEntity(test);
//
//		Entity canvas = new Entity();
//		canvas.add(new CanvasComponent());
//		canvas.add(new CanvasElementComponent());
//		canvas.add(new TransformComponent());
//		canvas.add(new DimensionComponent());
//		canvas.add(new LayerComponent(BitBuilder.none(32).s(0).get()));
//		canvas.add(new NameComponent().setName("Canvas"));
//		addEntity(canvas);

//		Interfacer interfacer = new Interfacer();
//
//		Entity transformDebug = interfacer.interfaceFor(cameraComponent);
//		relate(canvas, transformDebug);
//		addEntity(transformDebug, true);

		// WORLD TREE TEST

//		Entity worldEntity = new Entity();
//		worldEntity.add(new WorldComponent().setWorld(this));
//		worldEntity.add(new NameComponent().setName("World"));
//		addEntity(worldEntity);
//
//		Entity table = new Entity();
//		table.add(new CanvasElementComponent());
//		table.add(new TableComponent().setFillParent(true).setHorizontalAlignment(Enums.HorizontalAlignment.LEFT).setVerticalAlignment(Enums.VerticalAlignment.BOTTOM));
//		table.add(new TransformComponent());
//		table.add(new DimensionComponent());
//		table.add(new NameComponent().setName("Table"));
//		relate(canvas, table);
//		addEntity(table);
//
//		Entity tree = new Entity();
//		tree.add(new TransformComponent().setPosition(100,100,0));
//		tree.add(new DimensionComponent().setDimension(100,200).setOrigin(0, 0));
//		tree.add(new CanvasElementComponent());
//		tree.add(new TreeComponent());
//		tree.add(new TableCellComponent().setFillY(1)
//										 .setExpandY(1)
//										.setExpandX(1).setHorizontalAlignment(Enums.HorizontalAlignment.LEFT));
//		tree.add(new WorldTreeComponent());
//		tree.add(new IgnoredComponent());
//		addEntity(tree);
//		EntityBuilder.relate(table, tree);

//		Entity node1 = EntityBuilder.makeLabel("Entry 1").add(new TreeNodeComponent());
//		EntityBuilder.relate(tree, node1);
//		addEntity(node1);
//		Entity node2 = EntityBuilder.makeLabel("Entry 2").add(new TreeNodeComponent());
//		EntityBuilder.relate(tree, node2);
//		addEntity(node2);
//		Entity node21 = EntityBuilder.makeLabel("Entry 2 1")
//									.add(new TreeNodeComponent());
//		EntityBuilder.relate(node2, node21);
//		addEntity(node21);
//		Entity node22 = EntityBuilder.makeLabel("Entry 2 2")
//									.add(new TreeNodeComponent());
//		EntityBuilder.relate(node2, node22);
//		addEntity(node22);
//		Entity node3 = EntityBuilder.makeLabel("Entry 3").add(new TreeNodeComponent());
//		EntityBuilder.relate(tree, node3);
//		addEntity(node3);

		// TABLE TEST

//		Entity table = new Entity();
//		table.add(new CanvasElementComponent());
//		table.add(new TableComponent().setFillParent(true).setHorizontalAlignment(Enums.HorizontalAlignment.LEFT).setVerticalAlignment(Enums.VerticalAlignment.BOTTOM));
//		table.add(new TransformComponent());
//		table.add(new DimensionComponent());
//		relate(canvas, table);
//		addEntity(table);
//
//		Entity button = new Entity();
//		button.add(new TableCellComponent().setWidth(100).setHeight(100));
//		CanvasElementComponent element2 = new CanvasElementComponent();
//		button.add(element2);
//		button.add(new ButtonComponent());
//		TransformComponent trans = new TransformComponent();
//		button.add(trans.setX(0)
//						.setY(0)
//						.setZRotation(0)
//						.setScale(1f));
//		button.add(new DimensionComponent().setDimension(100, 100)
//										   .setOrigin(0f, 0f)
//										   .setPivot(0.5f, 0.5f));
//		relate(table, button);
//		addEntity(button);
//
//		Entity mainLabel = new Entity();
//		CanvasElementComponent element4 = new CanvasElementComponent();
//		mainLabel.add(element4);
//		mainLabel.add(new LabelComponent().setText("Hugo"));
//		TransformComponent trans4 = new TransformComponent();
//		mainLabel.add(trans4.setX(100)
//						.setY(200)
//						.setZRotation(0)
//						.setScale(1f));
//		mainLabel.add(new DimensionComponent().setDimension(100, 100)
//											  .setOrigin(0f, 0f)
//											  .setPivot(0.5f, 0.5f));
//		relate(button, mainLabel);
//		addEntity(mainLabel);
//
//		Entity label = makeLabel("Hugo");
//		relate(button, label);
//		addEntity(label);
//
//		Entity label2 = makeLabel("Another");
//		relate(button, label2);
//		addEntity(label2);

		// SCENE SAVING TEST
//		updateActiveScene();
//		saveActiveScene();
//
//		resetActiveScene();

		worldState = WorldState.RUNNING;
	}

	@Override
	public void resize(int width, int height) {
		int oldWidth = 0;
		int oldHeight = 0;
		if (worldBounds == null) {
			worldBounds = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		} else {
			oldWidth = (int) worldBounds.getWidth();
			oldHeight = (int) worldBounds.getHeight();
			worldBounds.setSize(width, height);
		}
		if (oldWidth != width || oldHeight != height) {
			dispatchEvent(new ResizeEvent(new Dimension(oldWidth, oldHeight), new Dimension(width, height)));
		}
	}

	@Override
	public void dispose() {
		decalBatch.dispose();
		cameraGroupStrategy.dispose();
	}

	private void render(float delta) {
		GL20 gl = Gdx.gl20;
		gl.glViewport(getX(), getY(), getWidth(), getHeight());
		gl.glScissor(getX(), getY(), getWidth(), getHeight());
		gl.glEnable(GL20.GL_SCISSOR_TEST);
		gl.glClearColor(0, 0, 0, 1);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		engine.update(delta);

		gl.glDisable(GL20.GL_SCISSOR_TEST);
		gl.glScissor(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	private void update(float delta) {
		eventEngine.update();
		tweenManager.update(delta);
	}

	@Override
	public void render() {
		if (worldState != WorldState.INITIALIZED) {
			if (Asset.getManager()
					 .update()) {
				if (loadingAssets) {
					loadingAssets = false;
					dispatchEvent(new AssetLoadingFinishedEvent());
				}
			} else {
				loadingAssets = true;
				LoadingTexture.recompute();
			}
			if (worldState.equals(WorldState.RUNNING)) {
				if (stableOnly) {
					for (EntitySystem system : engine.getSystems()) {
						AbstractSystem abstractSystem = (AbstractSystem) system;
						if (!abstractSystem.isStable() && abstractSystem.isActive()) {
							abstractSystem.setProcessing(true);
						}
					}
					stableOnly = false;
				}
				float delta = Gdx.graphics.getDeltaTime();
				float dt = delta * timeFlow;
				t += dt;
				update(dt);
				render(dt);
			} else if (worldState.equals(WorldState.PAUSED)) {
				if (!stableOnly) {
					for (EntitySystem system : engine.getSystems()) {
						AbstractSystem abstractSystem = (AbstractSystem) system;
						if (!abstractSystem.isStable() && abstractSystem.isActive()) {
							abstractSystem.setProcessing(false);
						}
					}
					stableOnly = true;
				}
				render(0);
			}
		}
	}


	/**
	 * Some methods to access the world bounds
	 */

	public int getWidth() {
		return (int) worldBounds.getWidth();
	}

	public int getHeight() {
		return (int) worldBounds.getHeight();
	}

	public Vector2 getAnchor() {
		return new Vector2((int) worldBounds.x, (int) worldBounds.y);
	}

	public int getX() {
		return (int) worldBounds.x;
	}

	public int getY() {
		return (int) worldBounds.y;
	}

	/**===============================================================================================================================**/

	/**
	 * Engine delegation methods
	 */

	public void pause() {
		worldState = WorldState.PAUSED;
	}

	@Override
	public void resume() {
		worldState = WorldState.RUNNING;
	}

	public void addEntity(Entity entity, boolean recursive) {
		addEntity(entity);
		if (recursive) {
			if (subs.has(entity)) {
				for (Entity subEntity : subs.get(entity)) {
					addEntity(subEntity, recursive);
				}
			}
		}
	}

	public void addEntity(Entity entity) {
		// update component ownership
		for (Component component : entity.getComponents()) {
			if (component instanceof ObservableComponent) {
				((ObservableComponent) component).setOwner(new WeakReference<>(entity));
			}
		}
		// if the added entity is a root entity make it a child of the world entity
		Entity worldEntity = worldEntityRef.get();
		if (worldEntity != null && !worldEntity.equals(entity) && !parents.has(entity)) {
			EntityUtilities.relate(worldEntity, entity);
		}
		engine.addEntity(entity);
	}

	public void addEntityListener(EntityListener listener) {
		engine.addEntityListener(listener);
	}

	public <T extends EntitySystem> T getSystem(Class<T> systemType) {
		T system = engine.getSystem(systemType);
		return system;
	}

	public void removeAllEntities() {
		engine.removeAllEntities();
	}

	public void removeEntity(Entity entity) {
		engine.removeEntity(entity);
	}

	public void removeSystem(EntitySystem system) {
		if (system instanceof EventListener) {
			unregister((EventListener) system);
		}
		engine.removeSystem(system);
	}

	public void removeEntityListener(EntityListener listener) {
		engine.removeEntityListener(listener);
	}

	public ImmutableArray<Entity> getEntitiesFor(Family family) {
		return engine.getEntitiesFor(family);
	}

	@SafeVarargs
	public final void addSystem(AbstractSystem system, Class<? extends Event>... events) {
		system.setWorld(this);
		if (system.isRenderSystem()) {
			// make sure render systems will be updated last
			system.priority = Integer.MIN_VALUE+1000;
		}
		engine.addSystem(system);
		if (events.length > 0) {
			system.setEvents(events);
			register((EventListener) system, events);
		}
	}

	public void removeAllSystems() {
		for (EntitySystem system : engine.getSystems()) {
			removeSystem(system);
		}
	}

	/**-------------------------------------------------------------------------------------------------------------------------------**/

	/**
	 * Event engine delegation methods
	 */

	@SafeVarargs
	public final void register(EventListener registrant,
							   Class<? extends Event>... events) {
		eventEngine.register(registrant, events);
	}

	public void unregister(EventListener unregistrant) {
		eventEngine.unregister(unregistrant);
	}

	@SafeVarargs
	public final void unregister(EventListener unregistrant,
								 Class<? extends Event>... events) {
		eventEngine.unregister(unregistrant, events);
	}

	public void dispatchEvent(Event event) {
		eventEngine.dispatchEvent(event);
	}

	/**-------------------------------------------------------------------------------------------------------------------------------**/

	/**
	 * Tween logicEngine methods
	 */

	public void startTimeline(Timeline timeline) {
		timeline.start(tweenManager);
	}

}
