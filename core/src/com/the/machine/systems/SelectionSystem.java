package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.SelectableComponent;
import com.the.machine.components.SelectionComponent;
import com.the.machine.components.SelectorComponent;
import com.the.machine.events.ClearSelectionEvent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.components.CameraComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.DisabledComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.input.TouchUpEvent;
import com.the.machine.framework.utility.EntityUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.badlogic.gdx.Input.Keys.*;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 14/03/15
 */
public class SelectionSystem
		extends AbstractSystem
		implements EntityListener, EventListener {

	transient private ComponentMapper<TransformComponent>    transforms  = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<SelectionComponent>    selected    = ComponentMapper.getFor(SelectionComponent.class);
	transient private ComponentMapper<SelectableComponent>   selectables = ComponentMapper.getFor(SelectableComponent.class);
	transient private ComponentMapper<SpriteRenderComponent> sprites     = ComponentMapper.getFor(SpriteRenderComponent.class);
	transient private ComponentMapper<CameraComponent> cameraComponents = ComponentMapper.getFor(CameraComponent.class);
	transient private ComponentMapper<LayerComponent>  layers           = ComponentMapper.getFor(LayerComponent.class);

	transient private ImmutableArray<Entity> selectorEntities   = null;
	transient private ImmutableArray<Entity> selectableEntities = null;
	transient private ImmutableArray<Entity> selectedEntities   = null;

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		selectorEntities = world.getEntitiesFor(Family.all(SelectorComponent.class, CameraComponent.class)
													  .get());
		Family selectableFamily = Family.all(TransformComponent.class, DimensionComponent.class, SelectableComponent.class)
										.exclude(DisabledComponent.class)
										.get();
		selectableEntities = world.getEntitiesFor(selectableFamily);
		selectedEntities = world.getEntitiesFor(Family.all(SelectionComponent.class)
													  .get());
		engine.addEntityListener(selectableFamily, this);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
		selectableEntities = null;
		selectorEntities = null;
		selectorEntities = null;
	}

	@Override
	public void entityAdded(Entity entity) {

	}

	@Override
	public void entityRemoved(Entity entity) {
		if (selected.has(entity)) {
			entity.remove(SelectionComponent.class);
		}
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof TouchUpEvent) {
			int screenX = ((TouchUpEvent) event).getScreenX();
			int screenY = ((TouchUpEvent) event).getScreenY();
			boolean multiSelect = Gdx.input.isKeyPressed(SHIFT_LEFT) || Gdx.input.isKeyPressed(SHIFT_RIGHT);
			List<Entity> hit = new ArrayList<>();
			if (selectorEntities != null && selectableEntities != null) {
				for (Entity selectorEntity : selectorEntities) {
					CameraComponent cameraComponent = cameraComponents.get(selectorEntity);
					for (Entity entity : selectableEntities) {
						LayerComponent layerComponent = null;
						if (layers.has(entity)) {
							layerComponent = layers.get(entity);
						}
						if (layerComponent == null || cameraComponent.getCullingMask() == null || cameraComponent.getCullingMask()
																												 .containsAll(layerComponent.getLayer())) {
							Rectangle boundingBox = EntityUtilities.getWorldBoundingBox(entity);
							Vector3 coordinates = EntityUtilities.getWorldCoordinates(screenX, screenY, selectorEntity, world);
							if (boundingBox.contains(coordinates.x, coordinates.y)) {
								hit.add(entity);
							}
						}
					}
				}
			}
			if (hit.size() > 1) {
				Collections.sort(hit, (e1, e2) -> (int) (transforms.get(e2)
																   .getZ() - transforms.get(e1)
																					   .getZ()));
			}
			if (!hit.isEmpty()) {
				Entity topEntity = hit.get(0);
				if (multiSelect && selectables.get(topEntity)
											  .isMultiSelect()) {
					if (selected.has(topEntity)) {
						deselect(topEntity);
					} else {
						select(topEntity);
					}
				} else {
					if (selected.has(topEntity)) {
						clearSelection();
					} else {
						clearSelection();
						select(topEntity);
					}
				}
			} else {
				clearSelection();
			}
		} else if (event instanceof ClearSelectionEvent) {
			clearSelection();
		}
	}

	private void deselect(Entity entity) {
		entity.remove(SelectionComponent.class);
		if (sprites.has(entity)) {
			sprites.get(entity)
				   .setTint(Color.WHITE);
			sprites.get(entity)
				   .notifyObservers();
		}
	}

	private void select(Entity entity) {
		entity.add(new SelectionComponent());
		if (sprites.has(entity)) {
			sprites.get(entity)
				   .setTint(Color.CYAN);
			sprites.get(entity)
				   .notifyObservers();
		}
	}

	private void clearSelection() {
		if (selectedEntities != null) {
			Entity[] entities = selectedEntities.toArray(Entity.class);
			for (Entity selectedEntity : entities) {
				deselect(selectedEntity);
			}
		}
	}
}
