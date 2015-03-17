package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.DragComponent;
import com.the.machine.components.DraggableComponent;
import com.the.machine.components.HandleComponent;
import com.the.machine.components.ResizableComponent;
import com.the.machine.components.SelectionComponent;
import com.the.machine.components.SelectorComponent;
import com.the.machine.components.ZoomIndependentComponent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.DisabledComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.ReferenceComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.physics.ColliderComponent;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.EntityUtilities;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 14/03/15
 */
public class ResizeHandleSystem
		extends AbstractSystem {

	transient private ComponentMapper<TransformComponent>    transforms       = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<DimensionComponent>    dimensions       = ComponentMapper.getFor(DimensionComponent.class);
	transient private ComponentMapper<DisabledComponent>     disabled         = ComponentMapper.getFor(DisabledComponent.class);
	transient private ComponentMapper<HandleComponent>       handleComponents = ComponentMapper.getFor(HandleComponent.class);
	transient private ComponentMapper<ColliderComponent>     colliders        = ComponentMapper.getFor(ColliderComponent.class);
	transient private ComponentMapper<DragComponent>    dragges       = ComponentMapper.getFor(DragComponent.class);
	transient private ComponentMapper<ReferenceComponent>    references       = ComponentMapper.getFor(ReferenceComponent.class);
	transient private ComponentMapper<CameraComponent>    cameraComponents       = ComponentMapper.getFor(CameraComponent.class);
	transient private ComponentMapper<LayerComponent>    layers       = ComponentMapper.getFor(LayerComponent.class);

	transient private ImmutableArray<Entity> handles          = null;
	transient private ImmutableArray<Entity> draggedHandles   = null;
	transient private ImmutableArray<Entity> handleables      = null;
	transient private ImmutableArray<Entity> selectorEntities = null;

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		handles = world.getEntitiesFor(Family.all(HandleComponent.class, TransformComponent.class, DimensionComponent.class)
											 .get());
		draggedHandles = world.getEntitiesFor(Family.all(HandleComponent.class, TransformComponent.class, DimensionComponent.class, DragComponent.class)
													.get());
		handleables = world.getEntitiesFor(Family.all(ResizableComponent.class, SelectionComponent.class, TransformComponent.class, DimensionComponent.class)
												 .get());
		selectorEntities = world.getEntitiesFor(Family.all(SelectorComponent.class, CameraComponent.class)
													  .get());
		HandleComponent.HandleType[] handleTypes = HandleComponent.HandleType.values();
		for (int i = 0; i < 8; i++) {
			Entity handle = new Entity();
			handle.add(new HandleComponent().setType(handleTypes[i]));
			handle.add(new DisabledComponent());
			handle.add(new DimensionComponent().setDimension(20, 20));
			handle.add(new TransformComponent().setZRotation((i / 2) * 90).setZ(100));
			DraggableComponent draggableComponent = new DraggableComponent();
			if (i == 1 || i == 5) {
				draggableComponent.setXAxis(false);
				handle.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch("handle_edge", TextureRegion.class)).setSortingLayer("World UI")
													  .setTint(Color.CYAN));
			} else if (i == 3 || i == 7) {
				draggableComponent.setYAxis(false);
				handle.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch("handle_edge", TextureRegion.class)).setSortingLayer("World UI")
													  .setTint(Color.CYAN));
			} else {
				handle.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch("handle_corner", TextureRegion.class)).setSortingLayer("World UI").setTint(Color.CYAN));
			}
			handle.add(draggableComponent);
			handle.add(new LayerComponent(BitBuilder.all(32)
													.c(0)
													.get()));
			handle.add(new ZoomIndependentComponent());
			world.addEntity(handle);
		}
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		for (Entity handle : handles) {
			world.removeEntity(handle);
		}
		handles = null;
		draggedHandles = null;
		handleables = null;
		selectorEntities = null;
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (draggedHandles.size() > 0) {
			Entity first = draggedHandles.first();
			HandleComponent handleComponent = handleComponents.get(first);
			Vector3 handlePos = transforms.get(first)
										 .getPosition();
			Vector3 delta = handlePos.cpy()
									  .sub(handleComponent
												   .getReferencePosition());
			Rectangle rect = handleComponent.getRect();
			Vector2 center = new Vector2();
			rect.getCenter(center);
			for (Entity entity : handleables) {
				TransformComponent at = EntityUtilities.computeAbsoluteTransform(entity);
				Rectangle bb = EntityUtilities.getWorldBoundingBox(entity);
				DimensionComponent dm = dimensions.get(entity);
				TransformComponent tf = transforms.get(entity);
				Vector3 pos = at.getPosition();
				double widthRatio = bb.getWidth() / rect.getWidth();
				double heightRatio = bb.getHeight() / rect.getHeight();
				double dx = delta.x * widthRatio;
				double dy = delta.y * heightRatio;
				double dtx;
				double dty;
				switch (handleComponent.getType()) {
					case TOP_LEFT:
						dtx = delta.x * (0.5f + ((center.x - pos.x) / rect.getWidth()));
						dty = delta.y * (0.5f - ((center.y - pos.y) / rect.getHeight()));
						if (dm.getHeight() + dy >= 1) {
							dm.setHeight((float) (dm.getHeight() + dy));
							tf.setY((float) (tf.getY() + dty));
						}
						if (dm.getWidth() + dx >= 1) {
							dm.setWidth((float) (dm.getWidth() - dx));
							tf.setX((float) (tf.getX() + dtx));
						}
						break;
					case TOP:
						dty = delta.y * (0.5f - ((center.y - pos.y) / rect.getHeight()));
						if (dm.getHeight() + dy >= 1) {
							dm.setHeight((float) (dm.getHeight() + dy));
							tf.setY((float) (tf.getY() + dty));
						}
						break;
					case TOP_RIGHT:
						dtx = delta.x * (0.5f - ((center.x - pos.x) / rect.getWidth()));
						dty = delta.y * (0.5f - ((center.y - pos.y) / rect.getHeight()));
						if (dm.getHeight() + dy >= 1) {
							dm.setHeight((float) (dm.getHeight() + dy));
							tf.setY((float) (tf.getY() + dty));
						}
						if (dm.getWidth() + dx >= 1) {
							dm.setWidth((float) (dm.getWidth() + dx));
							tf.setX((float) (tf.getX() + dtx));
						}
						break;
					case RIGHT:
						dtx = delta.x * (0.5f - ((center.x - pos.x) / rect.getWidth()));
						if (dm.getWidth() + dx >= 1) {
							dm.setWidth((float) (dm.getWidth() + dx));
							tf.setX((float) (tf.getX() + dtx));
						}
						break;
					case BOTTOM_RIGHT:
						dtx = delta.x * (0.5f - ((center.x - pos.x) / rect.getWidth()));
						dty = delta.y * (0.5f + ((center.y - pos.y) / rect.getHeight()));
						if (dm.getWidth() + dx >= 1) {
							dm.setWidth((float) (dm.getWidth() + dx));
							tf.setX((float) (tf.getX() + dtx));
						}
						if (dm.getHeight() - dy >= 1) {
							dm.setHeight((float) (dm.getHeight() - dy));
							tf.setY((float) (tf.getY() + dty));
						}
						break;
					case BOTTOM:
						dty = delta.y * (0.5f + ((center.y - pos.y) / rect.getHeight()));
						if (dm.getHeight() - dy >= 1) {
							dm.setHeight((float) (dm.getHeight() - dy));
							tf.setY((float) (tf.getY() + dty));
						}
						break;
					case BOTTOM_LEFT:
						dtx = delta.x * (0.5f + ((center.x - pos.x) / rect.getWidth()));
						dty = delta.y * (0.5f + ((center.y - pos.y) / rect.getHeight()));
						if (dm.getHeight() - dy >= 1) {
							dm.setHeight((float) (dm.getHeight() - dy));
							tf.setY((float) (tf.getY() + dty));
						}
						if (dm.getWidth() - dx >= 1) {
							dm.setWidth((float) (dm.getWidth() - dx));
							tf.setX((float) (tf.getX() + dtx));
						}
						break;
					case LEFT:
						dtx = delta.x * (0.5f + ((center.x - pos.x) / rect.getWidth()));
						if (dm.getWidth() - dx >= 1) {
							dm.setWidth((float) (dm.getWidth() - dx));
							tf.setX((float) (tf.getX() + dtx));
						}
						break;
				}
				dm.notifyObservers();
				tf.notifyObservers();
			}
		}
		if (handleables.size() > 0) {
			Rectangle bound = EntityUtilities.computeCommonBound(handleables);
			float wh = bound.getWidth() / 2;
			float hh = bound.getHeight() / 2;
			Vector2 center = new Vector2();
			bound.getCenter(center);
			for (int i = 0; i < 8; i++) {
				Vector3 pos = new Vector3(center.x, center.y, 0);
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
				HandleComponent handleComponent = handleComponents.get(handle);
				handleComponent
								.setRect(bound);
				handleComponent
								.setReferencePosition(pos);
				handle.remove(DisabledComponent.class);
				TransformComponent handleTransform = transforms.get(handle);
				handleTransform.setPosition(pos);
			}
		} else if (!disabled.has(handles.first())) {
			for (Entity handle : handles) {
				handle.add(new DisabledComponent());
			}
		}
	}
}
