package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.DragComponent;
import com.the.machine.components.DraggableComponent;
import com.the.machine.components.SelectionComponent;
import com.the.machine.components.SelectorComponent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.DisabledComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.input.TouchDownEvent;
import com.the.machine.framework.events.input.TouchDraggedEvent;
import com.the.machine.framework.events.input.TouchUpEvent;
import com.the.machine.framework.utility.EntityUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 14/03/15
 */
public class DraggingSystem
		extends AbstractSystem
		implements EventListener {

	transient private ComponentMapper<TransformComponent>     transforms       = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<SelectionComponent>     selected         = ComponentMapper.getFor(SelectionComponent.class);
	transient private ComponentMapper<DraggableComponent>     draggables       = ComponentMapper.getFor(DraggableComponent.class);
	transient private ComponentMapper<DragComponent>          drags            = ComponentMapper.getFor(DragComponent.class);
	transient private ComponentMapper<CanvasElementComponent> canvasElements   = ComponentMapper.getFor(CanvasElementComponent.class);
	transient private ComponentMapper<CameraComponent>        cameraComponents = ComponentMapper.getFor(CameraComponent.class);
	transient private ComponentMapper<LayerComponent>         layers           = ComponentMapper.getFor(LayerComponent.class);

	transient private ImmutableArray<Entity> selectorEntities  = null;
	transient private ImmutableArray<Entity> draggableEntities = null;
	transient private ImmutableArray<Entity> draggedEntities   = null;
	transient private ImmutableArray<Entity> selectedEntities  = null;

	transient private boolean dragging = false;

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		selectorEntities = world.getEntitiesFor(Family.all(SelectorComponent.class, CameraComponent.class)
													  .get());
		draggableEntities = world.getEntitiesFor(Family.all(TransformComponent.class, DimensionComponent.class, DraggableComponent.class)
													   .exclude(DisabledComponent.class)
													   .get());
		draggedEntities = world.getEntitiesFor(Family.all(DragComponent.class)
													 .get());
		selectedEntities = world.getEntitiesFor(Family.all(SelectionComponent.class)
													  .get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		selectorEntities = null;
		draggableEntities = null;
		draggedEntities = null;
		selectedEntities = null;
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof TouchDownEvent) {
			int screenX = ((TouchDownEvent) event).getScreenX();
			int screenY = ((TouchDownEvent) event).getScreenY();
			List<Entity> hit = new ArrayList<>();
			Map<Entity, Vector3> coords = new HashMap<>();
			if (selectorEntities != null) {
				for (Entity selectorEntity : selectorEntities) {
					CameraComponent cameraComponent = cameraComponents.get(selectorEntity);
					Vector3 coordinates = EntityUtilities.getWorldCoordinates(screenX, screenY, selectorEntity, world);
					for (Entity entity : draggableEntities) {
						LayerComponent layerComponent = null;
						if (layers.has(entity)) {
							layerComponent = layers.get(entity);
						}
						if (layerComponent == null || cameraComponent.getCullingMask() == null || cameraComponent.getCullingMask()
																												 .containsAll(layerComponent.getLayer())) {
							Rectangle boundingBox = EntityUtilities.getWorldBoundingBox(entity);
							if (boundingBox.contains(coordinates.x, coordinates.y)) {
								hit.add(entity);
								coords.put(entity, coordinates);
							}
						}
					}
				}
				if (hit.size() > 1) {
					Collections.sort(hit, (e1, e2) -> (canvasElements.has(e1) && canvasElements.has(e2))
													  ? 0
													  : (canvasElements.has(e1))
														? -1
														: (canvasElements.has(e2))
														  ? 1
														  : (int) (transforms.get(e2)
																			 .getZ() - transforms.get(e1)
																								 .getZ()));
				}
				if (!hit.isEmpty()) {
					Entity topEntity = hit.get(0);
					Vector3 worldCoords = coords.get(topEntity);
					if (selected.has(topEntity)) {
						for (Entity entity : selectedEntities) {
							Vector3 point = EntityUtilities.computeAbsoluteTransform(entity)
														   .getPosition();
							entity.add(new DragComponent().setDeltaToDragPoint(worldCoords.cpy().sub(point)).setDragPoint(worldCoords));
						}
					} else {
						Vector3 point = EntityUtilities.computeAbsoluteTransform(topEntity).getPosition();
						topEntity.add(new DragComponent().setDeltaToDragPoint(worldCoords.cpy()
																						 .sub(point))
														 .setDragPoint(worldCoords));
					}
				}
			}
		} else if (event instanceof TouchDraggedEvent) {
			if (draggedEntities.size() > 0) {
				int screenX = ((TouchDraggedEvent) event).getScreenX();
				int screenY = ((TouchDraggedEvent) event).getScreenY();
				dragging = true;
				for (Entity entity : draggedEntities) {
					DragComponent dragComponent = drags.get(entity);
					TransformComponent transformComponent = transforms.get(entity);
					for (Entity selectorEntity : selectorEntities) {
						CameraComponent cameraComponent = cameraComponents.get(selectorEntity);
						LayerComponent layerComponent = null;
						if (layers.has(entity)) {
							layerComponent = layers.get(entity);
						}
						if (layerComponent == null || cameraComponent.getCullingMask() == null || cameraComponent.getCullingMask()
																												 .containsAll(layerComponent.getLayer())) {
							Vector3 worldCoordinates = EntityUtilities.getWorldCoordinates(screenX, screenY, selectorEntity, world);
							Vector3 point = EntityUtilities.computeAbsoluteTransform(entity).getPosition();
							Vector3 coordinates = worldCoordinates.cpy().sub(point);
							dragComponent.setDragPoint(worldCoordinates);
							Vector3 dragPoint = dragComponent.getDeltaToDragPoint();
							Vector3 delta = coordinates.cpy()
													   .sub(dragPoint);
							DraggableComponent draggableComponent = draggables.get(entity);
							if (!draggableComponent.isXAxis()) {
								delta.x = 0;
							}
							if (!draggableComponent.isYAxis()) {
								delta.y = 0;
							}
							transformComponent.setPosition(transformComponent.getPosition()
																			 .cpy()
																			 .add(delta));
							transformComponent.notifyObservers();
						}
					}
				}
			}
		} else if (event instanceof TouchUpEvent) {
			for (Entity entity : draggedEntities) {
				entity.remove(DragComponent.class);
			}
			if (dragging) {
				event.setDrop(true);
				dragging = false;
			}
		}
	}
}
