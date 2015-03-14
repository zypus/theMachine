package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.DragComponent;
import com.the.machine.components.DraggableComponent;
import com.the.machine.components.HandleComponent;
import com.the.machine.components.ResizableComponent;
import com.the.machine.components.SelectionComponent;
import com.the.machine.components.SelectorComponent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.DisabledComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.ReferenceComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.ButtonComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.physics.ColliderComponent;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.EntityUtilities;

import java.lang.ref.WeakReference;

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
	transient private ComponentMapper<AreaComponent>         areas            = ComponentMapper.getFor(AreaComponent.class);
	transient private ComponentMapper<SpriteRenderComponent> sprites          = ComponentMapper.getFor(SpriteRenderComponent.class);
	transient private ComponentMapper<DisabledComponent>     disabled         = ComponentMapper.getFor(DisabledComponent.class);
	transient private ComponentMapper<HandleComponent>       handleComponents = ComponentMapper.getFor(HandleComponent.class);
	transient private ComponentMapper<ColliderComponent>     colliders        = ComponentMapper.getFor(ColliderComponent.class);
	transient private ComponentMapper<DraggableComponent>    draggables       = ComponentMapper.getFor(DraggableComponent.class);
	transient private ComponentMapper<ReferenceComponent>    references       = ComponentMapper.getFor(ReferenceComponent.class);

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
			CanvasElementComponent canvasElement = new CanvasElementComponent().setTouchable(Touchable.disabled);
			//			canvasElement.getListeners().add(new TouchListenerExecuter(() -> {handle.add(new DragComponent()); return true;}, null, null));
			handle.add(canvasElement);
			handle.add(new ButtonComponent());
			handle.add(new TransformComponent());
			handle.add(new ReferenceComponent());
			DraggableComponent draggableComponent = new DraggableComponent();
			if (i == 1 || i == 5) {
				draggableComponent.setXAxis(false);
			} else if (i == 3 || i == 7) {
				draggableComponent.setYAxis(false);
			}
			handle.add(draggableComponent);
			handle.add(new LayerComponent(BitBuilder.none(32)
													.s(0)
													.get()));
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
			Vector3 delta = transforms.get(first)
									  .getPosition()
									  .cpy()
									  .sub(handleComponent
												   .getReferencePosition());
			Rectangle rect = handleComponent.getRect();
			Vector2 center = new Vector2();
			rect.getCenter(center);
			Entity selector = (Entity) references.get(first)
								 .getReference()
								 .get();
			for (Entity entity : handleables) {
				TransformComponent at = EntityUtilities.computeAbsoluteTransform(entity);
				Rectangle bb = EntityUtilities.getWorldBoundingBox(entity);
				bb = EntityUtilities.toScreenRect(bb, selector, world);
				DimensionComponent dm = dimensions.get(entity);
				TransformComponent tf = transforms.get(entity);
				Vector3 pos = EntityUtilities.getScreenCoordinates(at.getPosition(), selector, world);
				float widthRatio = bb.getWidth() / rect.getWidth();
				float heightRatio = bb.getHeight() / rect.getHeight();
				float dx = delta.x * widthRatio;
				float dy = delta.y * heightRatio;
				float dtx;
				float dty;
				switch (handleComponent.getType()) {
					case TOP_LEFT:
						dtx = delta.x * (0.5f + ((center.x - pos.x) / rect.getWidth()));
						dty = delta.y * (0.5f - ((center.y - pos.y) / rect.getHeight()));
						if (dm.getHeight() + dy >= 1) {
							dm.setHeight(dm.getHeight() + dy);
							tf.setY(tf.getY() + dty);
						}
						if (dm.getWidth() + dx >= 1) {
							dm.setWidth(dm.getWidth() - dx);
							tf.setX(tf.getX() + dtx);
						}
						break;
					case TOP:
						dty = delta.y * (0.5f - ((center.y - pos.y) / rect.getHeight()));
						if (dm.getHeight() + dy >= 1) {
							dm.setHeight(dm.getHeight() + dy);
							tf.setY(tf.getY() + dty);
						}
						break;
					case TOP_RIGHT:
						dtx = delta.x * (0.5f - ((center.x - pos.x) / rect.getWidth()));
						dty = delta.y * (0.5f - ((center.y - pos.y) / rect.getHeight()));
						if (dm.getHeight() + dy >= 1) {
							dm.setHeight(dm.getHeight() + dy);
							tf.setY(tf.getY() + dty);
						}
						if (dm.getWidth() + dx >= 1) {
							dm.setWidth(dm.getWidth() + dx);
							tf.setX(tf.getX() + dtx);
						}
						break;
					case RIGHT:
						dtx = delta.x * (0.5f - ((center.x - pos.x) / rect.getWidth()));
						if (dm.getWidth() + dx >= 1) {
							dm.setWidth(dm.getWidth() + dx);
							tf.setX(tf.getX() + dtx);
						}
						break;
					case BOTTOM_RIGHT:
						dtx = delta.x * (0.5f - ((center.x - pos.x) / rect.getWidth()));
						dty = delta.y * (0.5f + ((center.y - pos.y) / rect.getHeight()));
						if (dm.getWidth() + dx >= 1) {
							dm.setWidth(dm.getWidth() + dx);
							tf.setX(tf.getX() + dtx);
						}
						if (dm.getHeight() - dy >= 1) {
							dm.setHeight(dm.getHeight() - dy);
							tf.setY(tf.getY() + dty);
						}
						break;
					case BOTTOM:
						dty = delta.y * (0.5f + ((center.y - pos.y) / rect.getHeight()));
						if (dm.getHeight() - dy >= 1) {
							dm.setHeight(dm.getHeight() - dy);
							tf.setY(tf.getY() + dty);
						}
						break;
					case BOTTOM_LEFT:
						dtx = delta.x * (0.5f + ((center.x - pos.x) / rect.getWidth()));
						dty = delta.y * (0.5f + ((center.y - pos.y) / rect.getHeight()));
						if (dm.getHeight() - dy >= 1) {
							dm.setHeight(dm.getHeight() - dy);
							tf.setY(tf.getY() + dty);
						}
						if (dm.getWidth() - dx >= 1) {
							dm.setWidth(dm.getWidth() - dx);
							tf.setX(tf.getX() + dtx);
						}
						break;
					case LEFT:
						dtx = delta.x * (0.5f + ((center.x - pos.x) / rect.getWidth()));
						if (dm.getWidth() - dx >= 1) {
							dm.setWidth(dm.getWidth() - dx);
							tf.setX(tf.getX() + dtx);
						}
						break;
				}
				if (colliders.has(entity)) {
					ColliderComponent colliderComponent = colliders.get(entity);
					ColliderComponent.Collider collider = colliderComponent.getColliders()
																		   .get(0);
					collider.setShape(dm.getWidth(), dm.getHeight());
				}
				tf.notifyObservers();
			}
		}
		if (handleables.size() > 0) {
			Rectangle bound = EntityUtilities.computeCommonBound(handleables);
			bound = EntityUtilities.toScreenRect(bound, selectorEntities.first(), world);
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
				handleComponents.get(handle)
								.setRect(bound);
				handleComponents.get(handle)
								.setReferencePosition(pos);
				references.get(handle)
						  .setReference(new WeakReference(selectorEntities.first()));
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
