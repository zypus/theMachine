package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.TimeUtils;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.DragComponent;
import com.the.machine.components.DraggableComponent;
import com.the.machine.components.HandleComponent;
import com.the.machine.components.ListenerComponent;
import com.the.machine.components.ResizableComponent;
import com.the.machine.components.SelectableComponent;
import com.the.machine.components.SelectionComponent;
import com.the.machine.components.SelectorComponent;
import com.the.machine.components.VelocityComponent;
import com.the.machine.events.MapEditorHotbarEvent;
import com.the.machine.events.MapEditorLoadEvent;
import com.the.machine.events.MapEditorLoadPrefabEvent;
import com.the.machine.events.MapEditorSaveEvent;
import com.the.machine.events.MapEditorSavePrefabEvent;
import com.the.machine.events.MapEditorSaveSuccessEvent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.DisabledComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.ReferenceComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.physics.ColliderComponent;
import com.the.machine.framework.components.physics.Light2dComponent;
import com.the.machine.framework.components.physics.Physics2dComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.input.KeyDownEvent;
import com.the.machine.framework.events.input.TouchUpEvent;
import com.the.machine.framework.interfaces.Observable;
import com.the.machine.framework.interfaces.Observer;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.EntityUtilities;

import java.util.ArrayList;
import java.util.List;

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
		implements EventListener, EntityListener, Observer {

	transient private ComponentMapper<NameComponent>         names            = ComponentMapper.getFor(NameComponent.class);
	transient private ComponentMapper<CameraComponent>       cameraComponents = ComponentMapper.getFor(CameraComponent.class);
	transient private ComponentMapper<TransformComponent>    transforms       = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<DimensionComponent>    dimensions       = ComponentMapper.getFor(DimensionComponent.class);
	transient private ComponentMapper<AreaComponent>         areas            = ComponentMapper.getFor(AreaComponent.class);
	transient private ComponentMapper<SpriteRenderComponent> sprites          = ComponentMapper.getFor(SpriteRenderComponent.class);
	transient private ComponentMapper<DisabledComponent>     disabled         = ComponentMapper.getFor(DisabledComponent.class);
	transient private ComponentMapper<ReferenceComponent>    references       = ComponentMapper.getFor(ReferenceComponent.class);
	transient private ComponentMapper<HandleComponent>       handleComponents = ComponentMapper.getFor(HandleComponent.class);
	transient private ComponentMapper<ColliderComponent>     colliders        = ComponentMapper.getFor(ColliderComponent.class);
	transient private ComponentMapper<Physics2dComponent>    bodies           = ComponentMapper.getFor(Physics2dComponent.class);

	transient private final double DOUBLE_TAP_TIME = 0.3f;
	transient private       double lastTap         = -2 * DOUBLE_TAP_TIME;

	transient private ImmutableArray<Entity> mapElements = null;
	transient private ImmutableArray<Entity> selected    = null;
	transient private ImmutableArray<Entity> selectedAgents    = null;
	transient private ImmutableArray<Entity> selectors   = null;

	transient private AreaComponent.AreaType currentType = WALL;
	transient private String prefabName = null;
	transient private boolean loadPrefab = false;

	transient private List<Entity> toBeRemoved = new ArrayList<>();

	public MapSystem() {
		super();
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(Family.all(DimensionComponent.class, AreaComponent.class)
									   .get(), this);
		mapElements = engine.getEntitiesFor(Family.all(TransformComponent.class, DimensionComponent.class)
												  .one(AreaComponent.class, Light2dComponent.class)
												  .get());
		selected = engine.getEntitiesFor(Family.all(AreaComponent.class)
											   .one(SelectionComponent.class, DragComponent.class)
											   .get());
		selectedAgents = engine.getEntitiesFor(Family.all(TransformComponent.class, SelectionComponent.class, Light2dComponent.class).get());
		selectors = engine.getEntitiesFor(Family.all(SelectorComponent.class, NameComponent.class, CameraComponent.class)
												.get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
		mapElements = null;
		selected = null;
		selectedAgents = null;
		selectors = null;
	}

	@Override
	public void entityAdded(Entity entity) {
		dimensions.get(entity)
					  .addObserver(this);
	}

	@Override
	public void entityRemoved(Entity entity) {
		dimensions.get(entity)
					  .deleteObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		DimensionComponent dm = (DimensionComponent) o;
		Entity entity = dm.getOwner()
										  .get();
		if (entity != null && colliders.has(entity)) {
			if (areas.get(entity).getType() == GROUND) {
				float wh = dm.getWidth() / 2;
				float hh = dm.getHeight() / 2;
				ColliderComponent colliderComponent = colliders.get(entity);
				List<ColliderComponent.Collider> colliderList = colliderComponent.getColliders();
				colliderList.get(0)
							.setShape(new Vector2(-wh, -hh), new Vector2(wh, -hh));
				colliderList.get(1)
							.setShape(new Vector2(wh, -hh), new Vector2(wh, hh));
				colliderList.get(2)
							.setShape(new Vector2(wh, hh), new Vector2(-wh, hh));
				colliderList.get(3)
							.setShape(new Vector2(-wh, hh), new Vector2(-wh, -hh));
			} else {
				ColliderComponent colliderComponent = colliders.get(entity);
				ColliderComponent.Collider collider = colliderComponent.getColliders()
																	   .get(0);
				collider.setShape(dm.getWidth(), dm.getHeight());
			}
		}
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof MapEditorSaveEvent) {
			Entity[] array = new Entity[mapElements.size()];
			int index = 0;
			for (Entity element : mapElements) {
				if (selected.contains(element, true)) {
					element.remove(SelectionComponent.class);
				}
				array[index] = element;
				index++;
			}
			world.savePrefab(((MapEditorSaveEvent) event).getName().concat(".map"), array);
			world.dispatchEvent(new MapEditorSaveSuccessEvent());
		} else if (event instanceof MapEditorLoadEvent) {
			Entity[] array = new Entity[mapElements.size()];
			int index = 0;
			for (Entity element : mapElements) {
				array[index] = element;
				index++;
			}
			Entity[] newEntities = world.loadPrefab(((MapEditorLoadEvent) event).getName().concat(".map"));
			if (newEntities != null && newEntities.length > 0) {
				for (Entity entity : array) {
					toBeRemoved.add(entity);
				}
			}
		} else if (event instanceof MapEditorSavePrefabEvent) {
			Entity[] array = new Entity[selected.size()];
			int index = 0;
			for (Entity element : selected) {
				if (!areas.has(element) || areas.get(element).getType() != GROUND ) {
					array[index] = element;
					index++;
				}
			}
			world.savePrefab(((MapEditorSavePrefabEvent) event).getName()
														 .concat(".obj"), array);
			world.dispatchEvent(new MapEditorSaveSuccessEvent());
		} else if (event instanceof MapEditorLoadPrefabEvent) {
			prefabName = ((MapEditorLoadPrefabEvent) event).getName();
			world.dispatchEvent(new MapEditorHotbarEvent(7));
		} else if (event instanceof MapEditorHotbarEvent) {
			int index = ((MapEditorHotbarEvent) event).getHotbarIndex();
			loadPrefab = false;
			switch (index) {
				case 0:
					currentType = WALL;
					break;
				case 1:
					currentType = WINDOW;
					break;
				case 2:
					currentType = DOOR_CLOSED;
					break;
				case 3:
					currentType = COVER;
					break;
				case 4:
					currentType = TOWER;
					break;
				case 5:
					currentType = TARGET;
					break;
				case 7:
					loadPrefab = true;
					break;
				default:
					break;
			}

			for (Entity entity : selected) {
				AreaComponent areaComponent = areas.get(entity);
				if (areaComponent.getType() != GROUND) {
					switch (index) {
						case 0:
							areaComponent.setType(WALL);
							entity.remove(Light2dComponent.class);
							break;
						case 1:
							if (areaComponent.getType() == WINDOW) {
								areaComponent.setType(WINDOW_BROKEN);
							} else {
								areaComponent.setType(WINDOW);
							}
							entity.remove(Light2dComponent.class);
							break;
						case 2:
							if (areaComponent.getType() == DOOR_CLOSED) {
								areaComponent.setType(DOOR_OPEN);
							} else {
								areaComponent.setType(DOOR_CLOSED);
							}
							break;
						case 3:
							areaComponent.setType(COVER);
							break;
						case 4:
							if (areaComponent.getType() == TOWER) {
								areaComponent.setType(TOWER_USED);
							} else {
								areaComponent.setType(TOWER);
							}
							break;
						case 5:
							areaComponent.setType(TARGET);
							break;
						case 6:
							// TODO agent / intruder
							break;
						case 7:
							break;
						default:
							System.out.println("Unsupported hotbar index of " + index);
					}
				}
			}
			for (Entity entity : selectedAgents) {
				if (index == 6) {
					// TODO agent / intruder
				}
			}
		} else if (event instanceof TouchUpEvent) {
			double tapTime = (double) TimeUtils.millis() / 1000f;
			boolean doubleTap = false;
			if (tapTime - lastTap < DOUBLE_TAP_TIME) {
				doubleTap = true;
				lastTap = -2 * DOUBLE_TAP_TIME;
			} else {
				lastTap = tapTime;
			}
			boolean control = Gdx.input.isKeyPressed(CONTROL_LEFT) || Gdx.input.isKeyPressed(CONTROL_RIGHT);
			if (doubleTap && ! control) {
				Entity camera = null;
				for (Entity selector : selectors) {
					NameComponent nameComponent = names.get(selector);
					if (nameComponent.getName()
									 .equals("Map Editor Camera")) {
						camera = selector;
						break;
					}
				}
				if (loadPrefab) {
					if (prefabName == null) {
						return;
					}
					Entity[] array = new Entity[mapElements.size()];
					int index = 0;
					for (Entity element : mapElements) {
						array[index] = element;
						index++;
					}
					Entity[] newEntities = world.loadPrefab(prefabName.concat(".obj"));
					if (camera != null) {
						Vector3 unproject = EntityUtilities.getWorldCoordinates(((TouchUpEvent) event).getScreenX(), ((TouchUpEvent) event).getScreenY(), camera, world);
						List<Entity> entityList = new ArrayList<>(newEntities.length);
						for (Entity entity : newEntities) {
							entityList.add(entity);
						}
						Rectangle bound = EntityUtilities.computeCommonBound(entityList);
						Vector2 center = new Vector2();
						bound.getCenter(center);
						Vector3 delta = unproject.cpy()
												 .sub(new Vector3(center, 0));
						delta.z = 0;
						for (Entity entity : entityList) {
							TransformComponent transformComponent = transforms.get(entity);
							transformComponent.setPosition(transformComponent.getPosition()
																			 .cpy()
																			 .add(delta));
							transformComponent.notifyObservers();
						}
					}
				} else {
					Entity newMapElement = new Entity();
					if (camera != null) {
						Vector3 unproject = EntityUtilities.getWorldCoordinates(((TouchUpEvent) event).getScreenX(), ((TouchUpEvent) event).getScreenY(), camera, world);
						newMapElement.add(new TransformComponent().setPosition(unproject)
																  .setZ(mapElements.size()));
					} else {
						newMapElement.add(new TransformComponent().setZ(mapElements.size()));
					}
					newMapElement.add(new DimensionComponent().setDimension(10, 10));
					newMapElement.add(new AreaComponent().setType(currentType));
					newMapElement.add(new LayerComponent(BitBuilder.none(32)
																   .s(1)
																   .get()));
					newMapElement.add(new SpriteRenderComponent().setTextureRegion(currentType.getTextureAsset())
																 .setSortingLayer("Default")
																 .setSortingOrder(mapElements.size() + 1));
					newMapElement.add(new Physics2dComponent().setType(BodyDef.BodyType.DynamicBody));
					newMapElement.add(new ColliderComponent().add(new ColliderComponent.Collider().setShape(new Rectangle(-5, -5, 10, 10))));
					newMapElement.add(new SelectableComponent());
					newMapElement.add(new ResizableComponent());
					newMapElement.add(new DraggableComponent());
					world.addEntity(newMapElement);
				}
			} else if (doubleTap) {
				Entity camera = null;
				for (Entity selector : selectors) {
					NameComponent nameComponent = names.get(selector);
					if (nameComponent.getName()
									 .equals("Map Editor Camera")) {
						camera = selector;
						break;
					}
				}
				Entity newAgent = new Entity();
				if (camera != null) {
					Vector3 unproject = EntityUtilities.getWorldCoordinates(((TouchUpEvent) event).getScreenX(), ((TouchUpEvent) event).getScreenY(), camera, world);
					newAgent.add(new TransformComponent().setPosition(unproject)
															  .setZ(mapElements.size()));
				} else {
					newAgent.add(new TransformComponent().setZ(mapElements.size()));
				}
				newAgent.add(new DimensionComponent().setDimension(2, 2));
				newAgent.add(new LayerComponent(BitBuilder.none(32)
															   .s(1)
															   .get()));
				newAgent.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch("badlogic", TextureRegion.class))
														.setSortingLayer("Default"));
				newAgent.add(new Physics2dComponent().setType(BodyDef.BodyType.DynamicBody));
				newAgent.add(new ColliderComponent().add(new ColliderComponent.Collider().setShape(new Vector2(), 1)));
				newAgent.add(new SelectableComponent());
				newAgent.add(new DraggableComponent());
				newAgent.add(new Light2dComponent().setType(Light2dComponent.LightType.CONE));
				newAgent.add(new VelocityComponent().setVelocity(10f));
				newAgent.add(new ListenerComponent());
				world.addEntity(newAgent);
			}
		} else if (event instanceof KeyDownEvent) {
			int keycode = ((KeyDownEvent) event).getKeycode();
			for (Entity agent : selectedAgents) {
				if (keycode == BACKSPACE) {
					toBeRemoved.add(agent);
				}
			}
			for (Entity entity : selected) {
				TransformComponent transformComponent = transforms.get(entity);
				if (keycode == BACKSPACE) {
					AreaComponent areaComponent = areas.get(entity);
					if (areaComponent.getType() != GROUND) {
						toBeRemoved.add(entity);
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
			switch (keycode) {
				case NUM_1:
					world.dispatchEvent(new MapEditorHotbarEvent(0));
					break;
				case NUM_2:
					world.dispatchEvent(new MapEditorHotbarEvent(1));
					break;
				case NUM_3:
					world.dispatchEvent(new MapEditorHotbarEvent(2));
					break;
				case NUM_4:
					world.dispatchEvent(new MapEditorHotbarEvent(3));
					break;
				case NUM_5:
					world.dispatchEvent(new MapEditorHotbarEvent(4));
					break;
				case NUM_6:
					world.dispatchEvent(new MapEditorHotbarEvent(5));
					break;
				case NUM_7:
					world.dispatchEvent(new MapEditorHotbarEvent(6));
					break;
				case NUM_8:
					world.dispatchEvent(new MapEditorHotbarEvent(7));
					break;
				default:
			}
		}

	}

	@Override
	public void update(float deltaTime) {
		if (!toBeRemoved.isEmpty()) {
			for (Entity entity : toBeRemoved) {
				world.removeEntity(entity);
			}
			toBeRemoved.clear();
		}
		if (mapElements != null) {
			for (Entity element : mapElements) {
				if (sprites.has(element)) {
					if (areas.has(element)) {
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
				if (bodies.has(element)) {
					Physics2dComponent physics2dComponent = bodies.get(element);
					if (selected.contains(element, true) && areas.has(element) && areas.get(element).getType() != GROUND) {
						physics2dComponent.setType(BodyDef.BodyType.DynamicBody);
					} else if (areas.has(element)){
						physics2dComponent.setType(BodyDef.BodyType.StaticBody);
					}
				}
			}
		}
	}

}
