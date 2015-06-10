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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
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
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.ObservableComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.WorldComponent;
import com.the.machine.framework.container.Scene;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventEngine;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.events.basic.ResizeEvent;
import com.the.machine.framework.events.input.KeyDownEvent;
import com.the.machine.framework.events.input.KeyTypedEvent;
import com.the.machine.framework.events.input.KeyUpEvent;
import com.the.machine.framework.events.input.MouseMovedEvent;
import com.the.machine.framework.events.input.ScrolledEvent;
import com.the.machine.framework.events.input.TouchDownEvent;
import com.the.machine.framework.events.input.TouchDraggedEvent;
import com.the.machine.framework.events.input.TouchUpEvent;
import com.the.machine.framework.serialization.EntitySerializer;
import com.the.machine.framework.systems.WorldSystem;
import com.the.machine.framework.utility.ClickEventListenerEventSpawner;
import com.the.machine.framework.utility.EntityUtilities;
import lombok.Getter;
import lombok.Setter;

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
public class World implements ApplicationListener, InputProcessor {

	private EventEngine  eventEngine;
	private Engine       engine;
	private TweenManager tweenManager;
	@Getter private InputMultiplexer inputMultiplexer = new InputMultiplexer();

	@Getter WeakReference<Entity> worldEntityRef;

	Kryo kryo = new Kryo();

	private Rectangle worldBounds;

	private WorldState worldState;

	private byte[] loadedData;
	private Scene activeScene;
	private byte[] activeData;

	@Getter private ImmutableArray<Bits> layers;
	@Getter private Map<String, Integer> sortingLayers = new HashMap<>();

	private ComponentMapper<ParentComponent>    parents    = ComponentMapper.getFor(ParentComponent.class);
	private ComponentMapper<SubEntityComponent> subs       = ComponentMapper.getFor(SubEntityComponent.class);
	private ComponentMapper<WorldComponent> worlds = ComponentMapper.getFor(WorldComponent.class);

	@Getter private CameraGroupStrategy cameraGroupStrategy;
	@Getter private DecalBatch          decalBatch;
	// physics
	@Getter @Setter private com.badlogic.gdx.physics.box2d.World box2dWorld = null;

	@Getter private float t        = 0;
	@Setter @Getter private         float timeFlow = 1f;

	private boolean stableOnly = false;

	private boolean loadingAssets = false;

	@Setter boolean render = true;

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
		kryo.addDefaultSerializer(ClickEventListenerEventSpawner.class, new ClickEventListenerEventSpawner.ClickEventListenerEventSpawnerSerializer(this));
	}

	public FileHandle locateScene(String name) {
		FileHandle internal = Gdx.files.local("");
		FileHandle[] list = internal.list("scenes");
		FileHandle scene;
		if (list.length > 0 && list[0].isDirectory()) {
			FileHandle sceneDir = list[0];
			scene = findFile(sceneDir, name + ".scene");
		} else {
			scene = findFile(internal, name + ".scene");
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

	public void savePrefab(String name, Entity... entities) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Output output = new Output(outputStream);
		kryo.writeObject(output, entities);
		output.close();
		byte[] bytes = outputStream.toByteArray();
		FileHandle fileHandle = newPrefabLocation(name);
		fileHandle.writeBytes(bytes, false);
	}

	public Entity[] loadPrefab(String name) {
		FileHandle prefab = locatePrefab(name);
		Entity[] entities = null;
		if (prefab != null) {
			byte[] data = prefab.readBytes();
			InputStream inputStream = new ByteArrayInputStream(data);
			Input input = new Input(inputStream);
			entities = kryo.readObject(input, Entity[].class);
			for (Entity entity : entities) {
				addEntity(entity, true);
			}
		} else {
			System.out.println("Prefab '" + name + "' not found.");
		}
		return entities;
	}

	private FileHandle locatePrefab(String name) {
		FileHandle internal = Gdx.files.local("");
		FileHandle[] list = internal.list("prefabs");
		FileHandle prefab;
		if (list.length > 0 && list[0].isDirectory()) {
			FileHandle prefabDir = list[0];
			prefab = findFile(prefabDir, name + ".prefab");
		} else {
			prefab = findFile(internal, name + ".prefab");
		}
		return prefab;
	}

	private FileHandle newPrefabLocation(String name) {
		FileHandle internal = Gdx.files.local("");
		FileHandle[] list = internal.list("prefabs");
		if (list.length == 0) {
			FileHandle scenes = Gdx.files.local("prefabs");
			scenes.mkdirs();
		}
		return Gdx.files.local("prefabs/" + name + ".prefab");
	}

	public void setActiveScene(byte[] data) {
		activeData = data;
		resetActiveScene();
	}

	public void resetActiveScene() {
		setup();
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
			if (worlds.has(entity)) {
				Entity previousWorld = worldEntityRef.get();
				WorldComponent worldComponent = worlds.get(entity);
				worldComponent.setWorld(worlds.get(previousWorld)
											  .getWorld());
				removeEntity(previousWorld);
				worldEntityRef = new WeakReference<>(entity);
				engine.addEntity(entity);
			} else {
				addEntity(entity);
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
		return Gdx.files.local("scenes/" + name + ".scene");
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
		engine.addEntity(worldEntity);
		addSystem(new WorldSystem());
	}

	public void buildScene(SceneBuilder builder) {
		WorldState previousState = worldState;
		worldState = WorldState.FULL_PAUSED;
//		setup();
		builder.createScene(this);
		worldState = previousState;
//		updateActiveScene();
//		saveActiveScene();
//
//		resetActiveScene();
	}

	@Override
	public void create() {
		if (worldBounds == null) {
			new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}

		inputMultiplexer.addProcessor(this);

		cameraGroupStrategy = new CameraGroupStrategy(new OrthographicCamera());
		decalBatch = new DecalBatch(cameraGroupStrategy);

		// the world entity itself need to be present all the time
		Entity worldEntity = new Entity();
		worldEntity.add(new WorldComponent().setWorld(this));
		worldEntity.add(new NameComponent().setName("World"));
		worldEntityRef = new WeakReference<>(worldEntity);
		engine.addEntity(worldEntity);

		addSystem(new WorldSystem());

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
				if (render) {
					render(dt);
				}
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
		engine.addEntity(entity);
//		Entity worldEntity = worldEntityRef.get();
//		if (worldEntity != null && !worldEntity.equals(entity) && !(parents.has(entity) || EntityUtilities.getParentMemory()
//																										  .containsKey(entity.getId()))) {
//			EntityUtilities.relate(worldEntity, entity);
//		}
	}

	public void addEntityListener(EntityListener listener) {
		engine.addEntityListener(listener);
	}

	public <T extends EntitySystem> T getSystem(Class<T> systemType) {
		T system = engine.getSystem(systemType);
		return system;
	}

	public void removeAllEntities() {
		for (Entity entity : engine.getEntities()) {
			if (parents.has(entity)) {
				Entity parent = parents.get(entity)
									   .getParent()
									   .get();
				if (parent != null && parent == worldEntityRef.get()) {
					Entity worldEntity = worldEntityRef.get();
					EntityUtilities.derelate(worldEntity, entity);
				}
			}
		}
		engine.removeAllEntities();
	}

	public void removeEntity(Entity entity) {
		if (parents.has(entity)) {
			Entity parent = parents.get(entity)
								   .getParent()
								   .get();
			if (parent != null && parent == worldEntityRef.get()) {
				Entity worldEntity = worldEntityRef.get();
				EntityUtilities.derelate(worldEntity, entity);
			}
		}
		engine.removeEntity(entity);
	}

	public void removeSystem(EntitySystem system) {
		if (system instanceof EventListener) {
			unregister((EventListener) system);
		}
		engine.removeSystem(system);
	}

	public void setSystemStatus(Class<? extends EntitySystem> systemClass, boolean enabled) {
		EntitySystem system = engine.getSystem(systemClass);
		if (system != null) {
			system.setProcessing(enabled);
		}
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

	/**-------------------------------------------------------------------------------------------------------------------------------**/
	/** input to event system translation																						      **/
	/**-------------------------------------------------------------------------------------------------------------------------------**/

	@Override
	public boolean keyDown(int keycode) {
		dispatchEvent(new KeyDownEvent(keycode));
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		dispatchEvent(new KeyUpEvent(keycode));
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		dispatchEvent(new KeyTypedEvent(character));
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		dispatchEvent(new TouchDownEvent(screenX, screenY, pointer, button));
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		dispatchEvent(new TouchUpEvent(screenX, screenY, pointer, button));
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		dispatchEvent(new TouchDraggedEvent(screenX, screenY, pointer));
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		dispatchEvent(new MouseMovedEvent(screenX, screenY));
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		dispatchEvent(new ScrolledEvent(amount));
		return false;
	}
}
