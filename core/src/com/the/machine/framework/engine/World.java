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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.assets.LoadingTexture;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.CanvasComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.DisabledComponent;
import com.the.machine.framework.components.IgnoredComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.ObservableComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.WorldComponent;
import com.the.machine.framework.components.WorldTreeComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.LabelComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.components.canvasElements.TableComponent;
import com.the.machine.framework.components.canvasElements.TreeComponent;
import com.the.machine.framework.container.Scene;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventEngine;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.events.basic.ResizeEvent;
import com.the.machine.framework.systems.canvas.CanvasElementSystem;
import com.the.machine.framework.systems.canvas.CanvasSystem;
import com.the.machine.framework.systems.canvas.TableCellSystem;
import com.the.machine.framework.systems.canvas.TableSystem;
import com.the.machine.framework.systems.canvas.TreeNodeSystem;
import com.the.machine.framework.systems.canvas.TreeSystem;
import com.the.machine.framework.systems.editor.WorldTreeSystem;
import com.the.machine.framework.systems.rendering.CameraRenderSystem;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.EntityBuilder;
import com.the.machine.framework.utility.Enums;
import com.the.machine.framework.utility.Interfacer;
import lombok.Getter;

import java.awt.Dimension;
import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created $(DATE)
 */
public class World implements ApplicationListener {

	private EventEngine  eventEngine;
	private Engine       logicEngine;
	private Engine 		 renderEngine;
	private TweenManager tweenManager;

	Kryo kryo = new Kryo();

	private Rectangle worldBounds;

	private WorldState worldState;

	private Scene loadedScene;
	private Scene activeScene;

	@Getter private ImmutableArray<Bits> layers;

	private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	private ComponentMapper<ParentComponent>    parents = ComponentMapper.getFor(ParentComponent.class);
	private ComponentMapper<SubEntityComponent>    subs = ComponentMapper.getFor(SubEntityComponent.class);
	private ComponentMapper<DisabledComponent>  disableds = ComponentMapper.getFor(DisabledComponent.class);

	@Getter private CameraGroupStrategy cameraGroupStrategy;
	@Getter private DecalBatch          decalBatch;

	@Getter private float t = 0;
	private float timeFlow = 1f;

	private boolean loadingAssets = false;

	public World() {
		this(null);
	}

	public World(Rectangle bounds) {
		worldState = WorldState.INITIALIZED;
		worldBounds = bounds;
		eventEngine = new EventEngine();
		logicEngine = new Engine();
		renderEngine = new Engine();
		tweenManager = new TweenManager();
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
			Input input = new Input(scene.read());
			loadedScene = kryo.readObject(input, Scene.class);
			input.close();
			setActiveScene(loadedScene);
		} else {
			System.out.println("Scene '" + name + "' not found.");
		}
	}

	public void reload() {
		setActiveScene(loadedScene);
	}

	public void setActiveScene(Scene scene) {
		activeScene = kryo.copy(scene);
		resetActiveScene();
	}

	public void resetActiveScene() {
		worldState = WorldState.PAUSED;
		removeAllEntities();
		removeAllSystems();
		layers = activeScene.getLayers();
		for (AbstractSystem system : activeScene.getSystems()) {
			addSystem(system);
		}
		for (Entity entity : activeScene.getEntities()) {
			addEntity(entity);
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
		return Gdx.files.local("scenes/"+name);
	}

	public void saveActiveScene() {
		FileHandle scene = locateScene(activeScene.getName());
		if (scene == null) {
			scene = newSceneLocation(activeScene.getName());
		}
		Output output = new Output(scene.write(false));
		kryo.writeObject(output, activeScene);
		output.flush();
	}

	public void updateActiveScene() {
		ImmutableArray<EntitySystem> logicEngineSystems = logicEngine.getSystems();
		ImmutableArray<EntitySystem> renderEngineSystems = renderEngine.getSystems();
		Array<AbstractSystem> systems = new Array<>(logicEngineSystems.size() + renderEngineSystems.size());
		for (EntitySystem system : logicEngineSystems) {
			systems.add((AbstractSystem) system);
		}
		for (EntitySystem system : renderEngineSystems) {
			systems.add((AbstractSystem) system);
		}
		activeScene.setSystems(kryo.copy(new ImmutableArray<AbstractSystem>(systems)));
		activeScene.setEntities(kryo.copy(logicEngine.getEntities()));
		activeScene.setLayers(kryo.copy(layers));
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

	@Override
	public void create() {
		if (worldBounds == null) {
			new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}

		cameraGroupStrategy = new CameraGroupStrategy(new OrthographicCamera());
		decalBatch = new DecalBatch(cameraGroupStrategy);

		// systems
		addSystem(new CameraRenderSystem(), AssetLoadingFinishedEvent.class);
		addSystem(new CanvasSystem(), ResizeEvent.class);
		addSystem(new CanvasElementSystem());
		addSystem(new TableSystem(), ResizeEvent.class);
		addSystem(new TableCellSystem());
		addSystem(new TreeSystem());
		addSystem(new TreeNodeSystem());
		addSystem(new WorldTreeSystem());

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
		addEntity(camera);

		Entity mapCamera = new Entity();
		mapCamera.add(new CameraComponent()
							  .setCullingMask(BitBuilder.all(32)
														.c(0)
														.get())
							  .setProjection(CameraComponent.Projection.ORTHOGRAPHIC)
							  .setZoom(0.05f)
							  .setViewportRect(new Rectangle(0.8f, 0.8f, 0.2f, 0.2f))
							  .setDepth(10)
							  .setBackground(new Color(0, 0.3f, 0.3f, 1)));
		mapCamera.add(new NameComponent().setName("Map Camera"));
		addEntity(mapCamera);

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
		uiCameraComponent.getClippingPlanes().setNear(-300f);
		uiCamera.add(uiCameraComponent);
		uiCamera.add(new TransformComponent().setPosition(0,0,0));
		uiCamera.add(new NameComponent().setName("UI Camera"));
		addEntity(uiCamera);

		Asset<TextureRegion> textureRegion = Asset.fetch("badlogic", TextureRegion.class);

		Entity test = new Entity();
		SpriteRenderComponent spriteRenderComponent = new SpriteRenderComponent().setTextureRegion(textureRegion).setSortingLayer("Default");
		test.add(new LayerComponent(BitBuilder.none(32).s(1).get()));
		test.add(spriteRenderComponent);
		TransformComponent spriteTransform = new TransformComponent().setPosition(new Vector3(0, 0, 0))
																	 .setZRotation(0)
																	 .setScale(1f);
		test.add(spriteTransform);
		SubEntityComponent subEntityComponent = new SubEntityComponent();
		test.add(subEntityComponent);
		test.add(new NameComponent().setName("Badlogic"));
		addEntity(test);

		Entity canvas = new Entity();
		canvas.add(new CanvasComponent());
		canvas.add(new CanvasElementComponent());
		canvas.add(new TransformComponent());
		canvas.add(new DimensionComponent());
		canvas.add(new LayerComponent(BitBuilder.none(32).s(0).get()));
		canvas.add(new NameComponent().setName("Canvas"));
		addEntity(canvas);

		Interfacer interfacer = new Interfacer();

		Entity transformDebug = interfacer.interfaceFor(spriteTransform);
		relate(canvas, transformDebug);
		addEntity(transformDebug, true);

		// WORLD TREE TEST

		Entity worldEntity = new Entity();
		worldEntity.add(new WorldComponent().setWorld(this));
		worldEntity.add(new NameComponent().setName("World"));
		addEntity(worldEntity);

		Entity table = new Entity();
		table.add(new CanvasElementComponent());
		table.add(new TableComponent().setFillParent(true).setHorizontalAlignment(Enums.HorizontalAlignment.LEFT).setVerticalAlignment(Enums.VerticalAlignment.BOTTOM));
		table.add(new TransformComponent());
		table.add(new DimensionComponent());
		table.add(new NameComponent().setName("Table"));
		relate(canvas, table);
		addEntity(table);

		Entity tree = new Entity();
		tree.add(new TransformComponent().setPosition(100,100,0));
		tree.add(new DimensionComponent().setDimension(100,200).setOrigin(0, 0));
		tree.add(new CanvasElementComponent());
		tree.add(new TreeComponent());
		tree.add(new TableCellComponent().setFillY(1)
										 .setExpandY(1)
										.setExpandX(1).setHorizontalAlignment(Enums.HorizontalAlignment.LEFT));
		tree.add(new WorldTreeComponent());
		tree.add(new IgnoredComponent());
		addEntity(tree);
		EntityBuilder.relate(table, tree);

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

		worldState = WorldState.RUNNING;
	}

	private Entity makeLabel(String text) {
		Entity label = new Entity();
		label.add(new CanvasElementComponent());
		label.add(new LabelComponent().setText(text));
		label.add(new TableCellComponent().setWidth(80).setHeight(20).setVerticalAlignment(Enums.VerticalAlignment.TOP));
		label.add(new TransformComponent().setX(0)
						.setY(0)
						.setZRotation(0)
						.setScale(1f));
		label.add(new DimensionComponent().setDimension(100, 20)
										  .setOrigin(0f, 0f)
										  .setPivot(0.5f, 0.5f));
		return label;
	}

	private void relate(Entity parent, Entity child) {
		if (!subs.has(parent)) {
			parent.add(new SubEntityComponent());
		}
		SubEntityComponent subEntityComponent = subs.get(parent);
		int i = subEntityComponent.size();
		if (!parents.has(child)) {
			child.add(new ParentComponent(parent, i));
		} else {
			ParentComponent parentComponent = parents.get(child);
			Entity formerParent = parentComponent.getParent();
			SubEntityComponent formerSubs = subs.get(formerParent);
			formerSubs.remove(parentComponent.getIndex());
			parentComponent.setParent(parent);
			parentComponent.setIndex(i);
		}
		subEntityComponent.add(child);
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

		renderEngine.update(delta);

		gl.glDisable(GL20.GL_SCISSOR_TEST);
		gl.glScissor(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	private void update(float delta) {
		eventEngine.update();
		tweenManager.update(delta);
		logicEngine.update(delta);
	}

	@Override
	public void render() {
		// first try to load than continue to the rest
//		if (!firstRenderPassed) {
//			Gdx.gl20.glClearColor(0, 0, 0, 1);
//			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
//		}
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
			float delta = Gdx.graphics.getDeltaTime();
			float dt = delta * timeFlow;
			t += dt;
			update(dt);
			render(dt);
		}
//		firstRenderPassed = true;
	}

	/**
	 * Utility methods
	 */

	public boolean isEntityEnabled(Entity entity) {
		if (disableds.has(entity)) {
			return false;
		} else {
			return !parents.has(entity) || isEntityEnabled(parents.get(entity)
																  .getParent());
		}
	}

	public TransformComponent computeAbsoluteTransform(Entity entity) {
		Vector3 position = new Vector3();
		Quaternion rotation = new Quaternion();
		Vector3 scale = new Vector3(1,1,1);
		if (transforms.has(entity)) {
			if (parents.has(entity)) {
				TransformComponent parentTransform = computeAbsoluteTransform(parents.get(entity)
																					 .getParent());
				position = parentTransform.getPosition().cpy();
				rotation = parentTransform.getRotation().cpy();
				scale = parentTransform.getScale().cpy();
			}
			TransformComponent transformComponent = transforms.get(entity);
			position.add(transformComponent.getPosition()
										   .cpy()
										   .scl(scale.x, scale.y, scale.z)
										   .mul(rotation));
			rotation.mul(transformComponent.getRotation());
			scale.x *= transformComponent.getXScale();
			scale.y *= transformComponent.getYScale();
			scale.z *= transformComponent.getZScale();
		}
		return new TransformComponent(position, rotation, scale);
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
				((ObservableComponent) component).setOwner(new WeakReference<Entity>(entity));
			}
		}
		logicEngine.addEntity(entity);
		renderEngine.addEntity(entity);
	}

	public void addEntityListener(EntityListener listener) {
		logicEngine.addEntityListener(listener);
		renderEngine.addEntityListener(listener);
	}

	public <T extends EntitySystem> T getSystem(Class<T> systemType) {
		T system = logicEngine.getSystem(systemType);
		if (system == null) {
			system = renderEngine.getSystem(systemType);
		}
		return system;
	}

	public void removeAllEntities() {
		logicEngine.removeAllEntities();
		renderEngine.removeAllEntities();
	}

	public void removeEntity(Entity entity) {
		logicEngine.removeEntity(entity);
		renderEngine.removeEntity(entity);
	}

	public void removeSystem(EntitySystem system) {
		logicEngine.removeSystem(system);
		renderEngine.removeSystem(system);
	}

	public void removeEntityListener(EntityListener listener) {
		logicEngine.removeEntityListener(listener);
		renderEngine.removeEntityListener(listener);
	}

	public ImmutableArray<Entity> getEntitiesFor(Family family) {
		return logicEngine.getEntitiesFor(family);
	}

	@SafeVarargs
	public final void addSystem(AbstractSystem system, Class<? extends Event>... events) {
		system.setWorld(this);
		if (system.isRenderSystem()) {
			renderEngine.addSystem(system);
		} else {
			logicEngine.addSystem(system);
		}
		if (events.length > 0) {
			register((EventListener) system, events);
		}
	}

	public void removeAllSystems() {
		for (EntitySystem system : logicEngine.getSystems()) {
			logicEngine.removeSystem(system);
		}
		for (EntitySystem system : renderEngine.getSystems()) {
			renderEngine.removeSystem(system);
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
