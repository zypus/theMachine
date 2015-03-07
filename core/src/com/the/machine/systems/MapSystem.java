package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.HandleComponent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.DisabledComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.ReferenceComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.ButtonComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.EntityUtilities;
import com.the.machine.framework.utility.Utils;

import java.lang.ref.WeakReference;

import static com.badlogic.gdx.Input.Keys.*;
import static com.the.machine.components.AreaComponent.AreaType.*;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 04/03/15
 */
public class MapSystem
		extends AbstractSystem
		implements InputProcessor {

	transient private ComponentMapper<NameComponent>         names            = ComponentMapper.getFor(NameComponent.class);
	transient private ComponentMapper<CameraComponent>       cameraComponents = ComponentMapper.getFor(CameraComponent.class);
	transient private ComponentMapper<TransformComponent>    transforms       = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<DimensionComponent>    dimensions       = ComponentMapper.getFor(DimensionComponent.class);
	transient private ComponentMapper<AreaComponent>         areas            = ComponentMapper.getFor(AreaComponent.class);
	transient private ComponentMapper<SpriteRenderComponent> sprites          = ComponentMapper.getFor(SpriteRenderComponent.class);
	transient private ComponentMapper<DisabledComponent>     disabled         = ComponentMapper.getFor(DisabledComponent.class);
	transient private ComponentMapper<ReferenceComponent>    references       = ComponentMapper.getFor(ReferenceComponent.class);
	transient private ComponentMapper<HandleComponent>       handleComponents = ComponentMapper.getFor(HandleComponent.class);

	transient private final float   DOUBLE_TAP_TIME = 0.3f;
	transient private       float   lastTap         = -2 * DOUBLE_TAP_TIME;
	transient private       Vector3 touchDownPoint  = null;

	transient private ImmutableArray<Entity> mapElements;
	transient private ImmutableArray<Entity> handles;
	transient private WeakReference<Entity>  mapCamera;
	transient private OrthographicCamera cam = new OrthographicCamera();

	transient private Vector2 cameraMovement = new Vector2();

	transient private Entity toDrag       = null;
	transient private Entity handleToDrag = null;

	transient private Entity selected = null;
	transient private Vector3 selectionDelta = new Vector3();

	public MapSystem() {
		super(Family.all()
					.get());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		world.getInputMultiplexer()
			 .addProcessor(this);
		mapElements = engine.getEntitiesFor(Family.all(TransformComponent.class, DimensionComponent.class, AreaComponent.class)
												  .get());
		findMapCamera();
		HandleComponent.HandleType[] handleTypes = HandleComponent.HandleType.values();
		for (int i = 0; i < 8; i++) {
			Entity handle = new Entity();
			handle.add(new HandleComponent().setType(handleTypes[i]));
			handle.add(new DisabledComponent());
			handle.add(new DimensionComponent().setDimension(20, 20));
			handle.add(new CanvasElementComponent());
			//			handle.add(new LabelComponent().setText("h"+i));
			handle.add(new ButtonComponent());
			handle.add(new TransformComponent());
			handle.add(new ReferenceComponent());
			world.addEntity(handle);
		}
	}

	private void findMapCamera() {
		ImmutableArray<Entity> cameras = world.getEntitiesFor(Family.one(CameraComponent.class)
																	.get());
		handles = world.getEntitiesFor(Family.one(HandleComponent.class)
											 .get());
		for (Entity camera : cameras) {
			if (names.has(camera)) {
				if (names.get(camera)
						 .getName()
						 .equals("Map Editor Camera")) {
					mapCamera = new WeakReference<>(camera);
				}
			}
		}
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		world.getInputMultiplexer()
			 .removeProcessor(this);
		mapElements = null;
		handles = null;
	}

	@Override
	public void update(float deltaTime) {
		if (mapCamera == null || mapCamera.get() == null) {
			findMapCamera();
		}
		if (mapCamera != null && mapCamera.get() != null) {
			Entity camera = mapCamera.get();
			TransformComponent camTransform = transforms.get(camera);
			camTransform.setX(camTransform.getX() + cameraMovement.x);
			camTransform.setY(camTransform.getY() + cameraMovement.y);
			CameraComponent cameraComponent = cameraComponents.get(camera);
			if (cameraComponent.getProjection() == CameraComponent.Projection.ORTHOGRAPHIC) {

				cam.near = cameraComponent.getClippingPlanes()
										  .getNear();
			}
			cam.far = cameraComponent.getClippingPlanes()
									 .getFar();
			TransformComponent transformComponent = EntityUtilities.computeAbsoluteTransform(camera);

			cam.direction.set(0, 0, -1);
			cam.up.set(0, 1, 0);
			cam.rotate(transformComponent.getRotation());
			Rectangle viewportRect = cameraComponent.getViewportRect();
			int width = (int) (world.getWidth() * viewportRect.getWidth());
			int height = (int) (world.getHeight() * viewportRect.getHeight());
			Vector2 origin = cameraComponent.getOrigin();
			cam.position.set(transformComponent.getPosition()
											   .cpy()
											   .add(width * (0.5f - origin.x), height * (0.5f - origin.y), 0));
			int x = (int) (world.getX() + world.getWidth() * viewportRect.getX());
			int y = (int) (world.getY() + world.getHeight() * viewportRect.getY());
			cam.viewportWidth = width;
			cam.viewportHeight = height;

			GL20 gl = Gdx.gl20;
			gl.glScissor(x, y, width, height);
			gl.glViewport(x, y, width, height);
		}
		if (mapElements != null) {
			for (Entity element : mapElements) {
				if (sprites.has(element)) {
					AreaComponent areaComponent = areas.get(element);
					if (areaComponent.isDirty()) {
						SpriteRenderComponent spriteRenderComponent = sprites.get(element);
						spriteRenderComponent
								.setTextureRegion(areaComponent.getType()
															   .getTextureAsset());
						spriteRenderComponent.setDirty(true);
						areaComponent.setDirty(false);
					}
				}
			}
		}
		if (selected != null && handles != null) {
			TransformComponent transform = EntityUtilities.computeAbsoluteTransform(selected);
			DimensionComponent dimensionComponent = dimensions.get(selected);
			float wh = dimensionComponent.getWidth() / 2;
			float hh = dimensionComponent.getHeight() / 2;
			for (int i = 0; i < 8; i++) {
				Vector3 pos = transform.getPosition()
									   .cpy();
				pos.z = 0;
				switch (i) {
					case 0:
						pos.add(-wh, hh, 0);
						break;
					case 1:
						pos.add(0, hh, 0);
						break;
					case 2:
						pos.add(wh, hh, 0);
						break;
					case 3:
						pos.add(wh, 0, 0);
						break;
					case 4:
						pos.add(wh, -hh, 0);
						break;
					case 5:
						pos.add(0, -hh, 0);
						break;
					case 6:
						pos.add(-wh, -hh, 0);
						break;
					case 7:
						pos.add(-wh, 0, 0);
						break;
				}
				Entity handle = handles.get(i);
				handleComponents.get(handle)
								.setReferencePosition(pos.cpy());
				Vector3 project = getScreenCoordinates(pos, mapCamera.get());
				references.get(handle)
						  .setReference(new WeakReference<>(selected));
				handle.remove(DisabledComponent.class);
				TransformComponent handleTransform = transforms.get(handle);
				handleTransform.setPosition(project);
			}
		} else {
			for (Entity handle : handles) {
				if (!disabled.has(handle)) {
					handle.add(new DisabledComponent());
				}
			}
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		if (selected != null) {
			TransformComponent transformComponent = transforms.get(selected);
			if (keycode == BACKSPACE) {
				AreaComponent areaComponent = areas.get(selected);
				if (areaComponent.getType() != GROUND) {
					world.removeEntity(selected);
					selected = null;
				}
			} else if (keycode == UP && Gdx.input.isKeyPressed(SHIFT_LEFT)) {
				transformComponent.setZ(MathUtils.clamp(transformComponent.getZ() + 1, 0, mapElements.size() - 1));
			} else if (keycode == DOWN && Gdx.input.isKeyPressed(SHIFT_LEFT)) {
				transformComponent.setZ(MathUtils.clamp(transformComponent.getZ() - 1, 0, mapElements.size() - 1));
			} else if (keycode == UP) {
				transformComponent.setY(transformComponent.getY() + 10);
			} else if (keycode == RIGHT) {
				transformComponent.setX(transformComponent.getX() + 10);
			} else if (keycode == DOWN) {
				transformComponent.setY(transformComponent.getY() - 10);
			} else if (keycode == LEFT) {
				transformComponent.setX(transformComponent.getX() - 10);
			}
			transformComponent.notifyObservers();
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		float tapTime = world.getT();
		boolean doubleTap = false;
		if (tapTime - lastTap < DOUBLE_TAP_TIME) {
			doubleTap = true;
			lastTap = -2 * DOUBLE_TAP_TIME;
		} else {
			lastTap = tapTime;
		}
		if (mapCamera != null && mapCamera.get() != null) {
			Entity camera = mapCamera.get();
			CameraComponent cc = cameraComponents.get(camera);
			Vector3 unproject = getWorldCoordinates(screenX, screenY, camera);
			touchDownPoint = unproject;
			boolean handleDragging = false;
			if (handles != null && selected != null) {
				for (Entity handle : handles) {
					if (!disabled.has(handle)) {
						DimensionComponent dimensionComponent = dimensions.get(handle);
						HandleComponent handleComponent = handleComponents.get(handle);
						Rectangle rect = new Rectangle(handleComponent.getReferencePosition().x - dimensionComponent.getWidth() * cc.getZoom() / 2, handleComponent.getReferencePosition().y - dimensionComponent.getHeight() * cc.getZoom() / 2, dimensionComponent.getWidth() * cc.getZoom(), dimensionComponent.getHeight() * cc.getZoom());
						if (rect.contains(unproject.x, unproject.y)) {
							handleToDrag = handle;
							handleDragging = true;
							break;
						}
					}
				}
			}
			if (!handleDragging && mapElements != null) {
				boolean found = false;
				boolean ground = false;
				for (Entity element : mapElements) {
					Rectangle rect = getWorldDimensions(element);
					AreaComponent areaComponent = areas.get(element);
					if (rect.contains(unproject.x, unproject.y)) {
						boolean grounded = areaComponent.getType() == GROUND;
						if (!grounded) {
							found = true;
							ground = false;
						} else {
							found = true;
							ground = true;
						}
						if (!grounded && doubleTap) {
							AreaComponent.AreaType type = areaComponent.getType();
							AreaComponent.AreaType[] constants = type.getDeclaringClass()
																	 .getEnumConstants();
							int length = constants.length;
							int ordinal = type
									.ordinal();
							ordinal++;
							if (ordinal >= length) {
								ordinal = 1;
							}
							areaComponent.setType(constants[ordinal]);
						} else {
							toDrag = element;
						}
						if (selected == null || selected != element) {
							TransformComponent transformComponent = EntityUtilities.computeAbsoluteTransform(element);
							selected = element;
							selectionDelta.set(unproject.cpy()
														.sub(transformComponent.getPosition()));
						}
					}
				}
				if ((!found || ground) && doubleTap) {
					Entity newMapElement = new Entity();
					newMapElement.add(new TransformComponent().setPosition(unproject)
															  .setZ(mapElements.size()));
					newMapElement.add(new DimensionComponent().setDimension(50, 50));
					AreaComponent.AreaType type = AreaComponent.AreaType.WALL;
					newMapElement.add(new AreaComponent().setType(type));
					newMapElement.add(new LayerComponent(BitBuilder.none(32)
																   .s(1)
																   .get()));
					newMapElement.add(new SpriteRenderComponent().setTextureRegion(type.getTextureAsset())
																 .setSortingLayer("Default"));
					selected = newMapElement;
					world.addEntity(newMapElement);
				}
				if (!found) {
					selected = null;
				}
			}
		}
		return false;
	}

	private Rectangle getWorldDimensions(Entity element) {
		TransformComponent transform = EntityUtilities.computeAbsoluteTransform(element);
		DimensionComponent dimensionComponent = dimensions.get(element);
		return new Rectangle(transform.getX() - dimensionComponent.getWidth() / 2, transform.getY() - dimensionComponent.getHeight() / 2, dimensionComponent.getWidth(), dimensionComponent.getHeight());
	}

	private Vector3 getWorldCoordinates(int screenX, int screenY, Entity camera) {
		TransformComponent absoluteTransform = EntityUtilities.computeAbsoluteTransform(camera);
		CameraComponent cameraComponent = cameraComponents.get(camera);
		Vector3 unproject = cam.unproject(new Vector3(screenX, screenY, 0), world.getX(), world.getY(), world.getWidth(), world.getHeight());
		unproject.scl(cameraComponent.getZoom())
				 .scl(world.getWidth() / 2, world.getHeight() / 2, 1)
				 .add(absoluteTransform.getPosition());
		return unproject;
	}

	private Vector3 getScreenCoordinates(Vector3 pos, Entity camera) {
		TransformComponent absoluteTransform = EntityUtilities.computeAbsoluteTransform(camera);
		CameraComponent cameraComponent = cameraComponents.get(camera);
		pos.sub(absoluteTransform.getPosition())
		   .scl(2f / world.getWidth(), 2f / world.getHeight(), 1)
		   .scl(1f / cameraComponent.getZoom());
		return cam.project(pos);
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		touchDownPoint = null;
		toDrag = null;
		handleToDrag = null;
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		lastTap = -2 * DOUBLE_TAP_TIME;
		if (mapCamera != null && mapCamera.get() != null) {
			Entity camera = mapCamera.get();
			Vector3 unproject = getWorldCoordinates(screenX, screenY, camera);
			Vector3 delta = unproject.cpy()
									 .sub(touchDownPoint);
			if (handleToDrag != null) {
				HandleComponent handleComponent = handleComponents.get(handleToDrag);
				ReferenceComponent referenceComponent = references.get(handleToDrag);
				Entity entity = referenceComponent.getReference()
												  .get();
				if (entity != null) {
					DimensionComponent dm = dimensions.get(entity);
					TransformComponent tf = transforms.get(entity);
					switch (handleComponent.getType()) {

						case TOP_LEFT:
							dm.setHeight(dm.getHeight() + delta.y);
							tf.setY(tf.getY() + delta.y / 2);
							dm.setWidth(dm.getWidth() - delta.x);
							tf.setX(tf.getX() + delta.x / 2);
							break;
						case TOP:
							dm.setHeight(dm.getHeight() + delta.y);
							tf.setY(tf.getY() + delta.y / 2);
							break;
						case TOP_RIGHT:
							dm.setHeight(dm.getHeight() + delta.y);
							tf.setY(tf.getY() + delta.y / 2);
							dm.setWidth(dm.getWidth() + delta.x);
							tf.setX(tf.getX() + delta.x / 2);
							break;
						case RIGHT:
							dm.setWidth(dm.getWidth() + delta.x);
							tf.setX(tf.getX() + delta.x / 2);
							break;
						case BOTTOM_RIGHT:
							dm.setWidth(dm.getWidth() + delta.x);
							tf.setX(tf.getX() + delta.x / 2);
							dm.setHeight(dm.getHeight() - delta.y);
							tf.setY(tf.getY() + delta.y / 2);
							break;
						case BOTTOM:
							dm.setHeight(dm.getHeight() - delta.y);
							tf.setY(tf.getY() + delta.y / 2);
							break;
						case BOTTOM_LEFT:
							dm.setHeight(dm.getHeight() - delta.y);
							tf.setY(tf.getY() + delta.y / 2);
							dm.setWidth(dm.getWidth() - delta.x);
							tf.setX(tf.getX() + delta.x / 2);
							break;
						case LEFT:
							dm.setWidth(dm.getWidth() - delta.x);
							tf.setX(tf.getX() + delta.x / 2);
							break;
					}
					tf.notifyObservers();
				}
			} else if (toDrag != null) {
				TransformComponent transformComponent = transforms.get(toDrag);
				transformComponent.setPosition(unproject.cpy()
														.add(selectionDelta));
				transformComponent.notifyObservers();
			}
			touchDownPoint = unproject;
		}

		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		if (screenX >= 0 && Utils.isInboundY(screenY, world.getY(), world.getHeight()) && screenX - world.getX() < 20) {
			cameraMovement.x = -1;
		} else if (screenX <= world.getWidth() && Utils.isInboundY(screenY, world.getX(), world.getHeight()) && world.getX() + world.getWidth() - screenX < 20) {
			cameraMovement.x = 1;
		} else {
			cameraMovement.x = 0;
		}
		if (screenY >= 0 && Utils.isInboundX(screenX, world.getX(), world.getWidth()) && screenY - world.getY() < 20) {
			cameraMovement.y = 1;
		} else if (screenY <= world.getHeight() && Utils.isInboundX(screenX, world.getX(), world.getWidth()) && world.getY() + world.getHeight() - screenY < 20) {
			cameraMovement.y = -1;
		} else {
			cameraMovement.y = 0;
		}
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		if (mapCamera != null && mapCamera.get() != null) {
			Entity camera = mapCamera.get();
			CameraComponent cameraComponent = cameraComponents.get(camera);
			cameraComponent.setZoom(cameraComponent.getZoom() + 0.1f * amount);
		}
		return false;
	}
}
