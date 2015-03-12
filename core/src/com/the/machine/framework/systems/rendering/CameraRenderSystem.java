package com.the.machine.framework.systems.rendering;

import box2dLight.RayHandler;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Array;
import com.the.machine.framework.SortedIteratingSystem;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.CanvasComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.physics.Light2dRenderComponent;
import com.the.machine.framework.components.physics.Physics2dDebugComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.interfaces.Observable;
import com.the.machine.framework.interfaces.Observer;
import com.the.machine.framework.utility.EntityUtilities;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 11/02/15
 */
public class CameraRenderSystem
		extends SortedIteratingSystem
		implements EntityListener, Observer, EventListener {

	//TODO CameraListener, if the depth of a camera changes, camera order needs to be updated

	@Getter private transient ComponentMapper<CameraComponent>       cameraComponents = ComponentMapper.getFor(CameraComponent.class);
	private transient         ComponentMapper<TransformComponent>    transforms       = ComponentMapper.getFor(TransformComponent.class);
	private transient         ComponentMapper<DimensionComponent>    dimensions       = ComponentMapper.getFor(DimensionComponent.class);
	private transient         ComponentMapper<SpriteRenderComponent> spriteRenderers  = ComponentMapper.getFor(SpriteRenderComponent.class);
	private transient         ComponentMapper<CanvasComponent>       canvas           = ComponentMapper.getFor(CanvasComponent.class);
	private transient         ComponentMapper<LayerComponent>        layers           = ComponentMapper.getFor(LayerComponent.class);
	private transient         ComponentMapper<Physics2dDebugComponent>        physic2dDebugs           = ComponentMapper.getFor(Physics2dDebugComponent.class);
	private transient         ComponentMapper<Light2dRenderComponent> lights = ComponentMapper.getFor(Light2dRenderComponent.class);

	private transient OrthographicCamera orthoCam = new OrthographicCamera();
	private transient PerspectiveCamera  persCam  = new PerspectiveCamera();

	private transient Family                 cameraFamily;
	private transient ImmutableArray<Entity> cameras;
	private transient Array<Entity>          sortedCameras;
	private transient Comparator<Entity>     cameraComparator;

	private transient ShapeRenderer      shapeRenderer;
	private transient Box2DDebugRenderer box2DDebugRenderer;

	private transient Map<Entity, Decal> decalMap = new HashMap<>();

	@Override
	public boolean isRenderSystem() {
		return true;
	}

	@Override
	public boolean isStable() {
		return true;
	}

	public CameraRenderSystem() {
		super(Family.one(SpriteRenderComponent.class, CanvasComponent.class, Physics2dDebugComponent.class, Light2dRenderComponent.class)
					.get());
		setComparator(new LayerComparator(this).thenComparing(new OrderComparator(this)));

		cameraFamily = Family.one(CameraComponent.class)
							 .get();
		sortedCameras = new Array<>(false, 16);
		cameras = new ImmutableArray<>(sortedCameras);
		cameraComparator = new CameraComparator(this);
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		ImmutableArray<Entity> newEntities = engine.getEntitiesFor(cameraFamily);
		sortedCameras.clear();
		if (newEntities.size() > 0) {
			for (int i = 0; i < newEntities.size(); ++i) {
				sortedCameras.add(newEntities.get(i));
			}
			sortedCameras.sort(cameraComparator);
		}
		engine.addEntityListener(this);
		shapeRenderer = new ShapeRenderer();
		box2DDebugRenderer = new Box2DDebugRenderer();
		world.getSortingLayers()
			 .put("UI", 1);
		world.getSortingLayers()
			 .put("Physics 2d Debug", 2);
		world.getSortingLayers()
			 .put("Lights 2d", 3);
		world.getSortingLayers()
			 .put("Default", 4);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
		sortedCameras.clear();
	}

	@Override
	public void entityAdded(Entity entity) {
		if (cameraFamily.matches(entity)) {
			if (cameraComponents.has(entity)) {
				cameraComponents.get(entity)
								.addObserver(this);
			}
			sortedCameras.add(entity);
			sortedCameras.sort(cameraComparator);
		} else if (family.matches(entity)) {
			if (spriteRenderers.has(entity)) {
				SpriteRenderComponent spriteRenderComponent = spriteRenderers.get(entity);
				Decal sprite = Decal.newDecal(1, 1, spriteRenderComponent.getTextureRegion()
																		 .get(), true);
				decalMap.put(entity, sprite);
				spriteRenderComponent.addObserver(this);
			}
			super.entityAdded(entity);
		}
	}

	@Override
	public void entityRemoved(Entity entity) {
		if (cameraFamily.matches(entity)) {
			if (cameraComponents.has(entity)) {
				cameraComponents.get(entity)
								.deleteObserver(this);
			}
			sortedCameras.removeValue(entity, true);
		} else if (family.matches(entity)) {
			if (spriteRenderers.has(entity)) {
				spriteRenderers.get(entity)
							   .deleteObserver(this);
			}
			decalMap.remove(entity);
			super.entityRemoved(entity);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof CameraComponent) {
			sortedCameras.sort(cameraComparator);
		} else if (o instanceof SpriteRenderComponent) {
			SpriteRenderComponent src = (SpriteRenderComponent) o;
			Entity entity = src.getOwner()
							   .get();
			if (entity != null) {
				Decal decal = decalMap.get(entity);
				if (decal != null) {
					decal.setTextureRegion(src.getTextureRegion()
											  .get());
					decal.setColor(src.getTint());
				}
			}
		}
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof AssetLoadingFinishedEvent) {
			for (Entity entity : getEntities()) {
				Decal decal = decalMap.get(entity);
				if (decal != null) {
					decal.setTextureRegion(spriteRenderers.get(entity)
														  .getTextureRegion()
														  .get());
				}
			}
		}
	}

	@Override
	public void update(float deltaTime) {
		Array<Entity> entities = sortedEntities;
		for (int c = 0; c < cameras.size(); c++) {
			Entity cameraEntity = sortedCameras.get(c);
			CameraComponent cameraComponent = cameraComponents.get(cameraEntity);
			Camera camera;
			if (cameraComponent.getProjection() == CameraComponent.Projection.ORTHOGRAPHIC) {
				camera = orthoCam;
				orthoCam.zoom = cameraComponent.getZoom();
				camera.near = cameraComponent.getClippingPlanes()
											 .getNear();
			} else {
				camera = persCam;
				persCam.fieldOfView = cameraComponent.getFieldOfView();
				if (cameraComponent.getClippingPlanes()
								   .getNear() == 0) {
					camera.near = 0.001f;
				} else {
					camera.near = cameraComponent.getClippingPlanes()
												 .getNear();
				}
			}
			camera.far = cameraComponent.getClippingPlanes()
										.getFar();
			TransformComponent transformComponent = EntityUtilities.computeAbsoluteTransform(cameraEntity);

			camera.direction.set(0, 0, -1);
			camera.up.set(0, 1, 0);
			camera.rotate(transformComponent.getRotation());
			Rectangle viewportRect = cameraComponent.getViewportRect();
			int width = (int) (world.getWidth() * viewportRect.getWidth());
			int height = (int) (world.getHeight() * viewportRect.getHeight());
			Vector2 origin = cameraComponent.getOrigin();
			camera.position.set(transformComponent.getPosition()
												  .cpy()
												  .add(width * (0.5f - origin.x), height * (0.5f - origin.y), 0));
			int x = (int) (world.getX() + world.getWidth() * viewportRect.getX());
			int y = (int) (world.getY() + world.getHeight() * viewportRect.getY());
			camera.viewportWidth = width;
			camera.viewportHeight = height;

			GL20 gl = Gdx.gl20;
			gl.glScissor(x, y, width, height);
			gl.glViewport(x, y, width, height);
			Color background = cameraComponent.getBackground();
			gl.glClearColor(background.r, background.g, background.b, background.a);
			gl.glClear(cameraComponent.getClearFlag());

			world.getCameraGroupStrategy()
				 .setCamera(camera);
			DecalBatch decalBatch = world.getDecalBatch();

			camera.update();

			// draw all entities
			List<Entity> onTop = new ArrayList<>();
			for (int i = 0; i < entities.size; i++) {
				Entity entity = entities.get(i);
				if (EntityUtilities.isEntityEnabled(entity)) {
					LayerComponent layerComponent = null;
					if (layers.has(entity)) {
						layerComponent = layers.get(entity);
					}
					if (layerComponent == null || cameraComponent.getCullingMask() == null || cameraComponent.getCullingMask()
																											 .containsAll(layerComponent.getLayer())) {
						if (spriteRenderers.has(entity) && spriteRenderers.get(entity)
																		  .isEnabled()) {
							gl.glEnable(GL20.GL_DEPTH_TEST);
							SpriteRenderComponent spriteRenderComponent = spriteRenderers.get(entity);
							Decal sprite;
							if (decalMap.containsKey(entity)) {
								sprite = decalMap.get(entity);
								if (spriteRenderComponent.isDirty()) {
									sprite.setTextureRegion(spriteRenderComponent.getTextureRegion()
																				 .get());
								}
								TransformComponent spriteTransform = EntityUtilities.computeAbsoluteTransform(entity);
								sprite.setPosition(spriteTransform.getPosition());
								sprite.setRotation(spriteTransform.getRotation());
								if (dimensions.has(entity)) {
									DimensionComponent dimensionComponent = dimensions.get(entity);
									sprite.setScale(spriteTransform.getXScale() * dimensionComponent.getWidth(), spriteTransform.getYScale() * dimensionComponent.getHeight());
								} else {
									sprite.setScale(spriteTransform.getXScale(), spriteTransform.getYScale());
								}
								//													sprite.setRotation(new Vector3(-camera.direction.x, -camera.direction.y, -camera.direction.z), Vector3.Y);
								decalBatch.add(sprite);
							}
						}
						// draw canvas
						if (canvas.has(entity) && canvas.get(entity)
														.isEnabled()) {
							CanvasComponent canvasComponent = canvas.get(entity);
							canvasComponent.getStage()
										   .getViewport()
										   .setCamera(camera);
							canvasComponent.getStage()
										   .draw();
							if (canvasComponent.isDebug()) {
								canvasComponent.getStage()
											   .getRoot()
											   .setDebug(true, true);
							}
							canvasComponent.getStage()
										   .getRoot()
										   .setTransform(true);
							shapeRenderer.setProjectionMatrix(camera.combined);
							shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
							canvasComponent.getStage()
										   .getRoot()
										   .drawDebug(shapeRenderer);
							shapeRenderer.end();
							gl.glScissor(x, y, width, height);
							gl.glViewport(x, y, width, height);
						}

						if (physic2dDebugs.has(entity) && physic2dDebugs.get(entity).isEnabled()) {
							onTop.add(entity);
						}

						if (lights.has(entity) && lights.get(entity)
														.isEnabled()) {
							onTop.add(entity);
						}
					}
				}
			}
			decalBatch.flush();
			for (int i = onTop.size()-1; i >= 0; i--) {
				Entity entity = onTop.get(i);
				if (physic2dDebugs.has(entity)) {
					Physics2dDebugComponent physics2dDebugComponent = physic2dDebugs.get(entity);
					Matrix4 projection = camera.combined.cpy();
					projection.scl(physics2dDebugComponent.getBoxToWorld(), physics2dDebugComponent.getBoxToWorld(), 1f);
					box2DDebugRenderer.render(physics2dDebugComponent.getBox2dWorld(), projection);
				}
				if (lights.has(entity)) {
					Light2dRenderComponent lc = lights.get(entity);
					RayHandler rayHandler = lc.getRayHandler();
					RayHandler.setGammaCorrection(lc.isGammaCorrection());
					RayHandler.useDiffuseLight(lc.isUseDiffuseLights());
					Color ambientLight = lc.getAmbientLight();
					rayHandler.setAmbientLight(ambientLight.r, ambientLight.g, ambientLight.b, ambientLight.a);
					rayHandler.setBlur(lc.isBlur());
					rayHandler.setBlurNum(lc.getBlurNum());
					Matrix4 projection = camera.combined.cpy();
					projection.scl(10, 10, 1f);
					rayHandler.setCombinedMatrix(projection);
					rayHandler.render();
				}
			}
			onTop.clear();
		}

	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {

	}

	protected static class LayerComparator
			implements Comparator<Entity> {

		private CameraRenderSystem cameraRenderSystem;

		private LayerComparator(CameraRenderSystem cameraRenderSystem) {
			this.cameraRenderSystem = cameraRenderSystem;
		}

		@Override
		public int compare(Entity o1, Entity o2) {
			LayerSortable ls1 = cameraRenderSystem.getSortable(o1);
			LayerSortable ls2 = cameraRenderSystem.getSortable(o2);
			int sortingLayer1 = (ls1 != null && ls1.getSortingLayer() != null)
								? cameraRenderSystem.world.getSortingLayers()
														  .get(ls1.getSortingLayer())
								: Integer.MIN_VALUE;
			int sortingLayer2 = (ls2 != null && ls2.getSortingLayer() != null)
								? cameraRenderSystem.world.getSortingLayers()
														  .get(ls2.getSortingLayer())
								: Integer.MIN_VALUE;
			return sortingLayer1 - sortingLayer2;
		}
	}

	private LayerSortable getSortable(Entity o1) {
		if (spriteRenderers.has(o1)) {
			return spriteRenderers.get(o1);
		}
		return null;
	}

	protected static class OrderComparator
			implements Comparator<Entity> {

		private CameraRenderSystem cameraRenderSystem;

		private OrderComparator(CameraRenderSystem cameraRenderSystem) {
			this.cameraRenderSystem = cameraRenderSystem;
		}

		@Override
		public int compare(Entity o1, Entity o2) {
			LayerSortable ls1 = cameraRenderSystem.getSortable(o1);
			LayerSortable ls2 = cameraRenderSystem.getSortable(o2);
			int sortingOrder1 = (ls1 != null)
								? ls1.getSortingOrder()
								: Integer.MIN_VALUE;
			int sortingOrder2 = (ls2 != null)
								? ls2.getSortingOrder()
								: Integer.MIN_VALUE;
			return sortingOrder1 - sortingOrder2;
		}
	}

	protected static class CameraComparator
			implements Comparator<Entity> {

		private CameraRenderSystem cameraRenderSystem;

		private CameraComparator(CameraRenderSystem cameraRenderSystem) {
			this.cameraRenderSystem = cameraRenderSystem;
		}

		@Override
		public int compare(Entity o1, Entity o2) {
			CameraComponent camera1 = cameraRenderSystem.cameraComponents.get(o1);
			CameraComponent camera2 = cameraRenderSystem.cameraComponents.get(o2);
			return camera1.getDepth() - camera2.getDepth();
		}
	}
}
