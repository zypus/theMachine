package com.the.machine.framework.systems.canvas;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.CanvasComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.basic.ResizeEvent;
import com.the.machine.framework.utility.EntityUtilities;
import lombok.Getter;

import java.awt.Dimension;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/02/15
 */
public class CanvasSystem extends IteratingSystem
		implements EntityListener, EventListener {

	transient private ComponentMapper<CanvasComponent> canvasComponents = ComponentMapper.getFor(CanvasComponent.class);
	transient private ComponentMapper<CanvasElementComponent> canvasElements = ComponentMapper.getFor(CanvasElementComponent.class);
	transient private ComponentMapper<ParentComponent> parents = ComponentMapper.getFor(ParentComponent.class);

	@Getter transient private Array<Entity> elementsToAdd = new Array<>();

	public CanvasSystem() {
		super(Family.all(CanvasComponent.class, CanvasElementComponent.class)
					.get(), 0);
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(Family.one(CanvasComponent.class)
									   .get(), this);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}

	@Override
	public void entityAdded(Entity entity) {
		if (canvasComponents.has(entity)) {
			CanvasComponent canvasComponent = canvasComponents.get(entity);
			canvasComponent.setBatch(new SpriteBatch());
			Stage stage = new Stage();
			world.getInputMultiplexer()
				 .addProcessor(0, stage);
			canvasComponent.setStage(stage);

			CanvasElementComponent elementComponent = canvasElements.get(entity);
			elementComponent.setActor(stage.getRoot());
			elementComponent.setGroup(true);
			elementComponent.setAdded(true);
		}
	}

	@Override
	public void entityRemoved(Entity entity) {
		if (canvasComponents.has(entity)) {
			CanvasComponent canvasComponent = canvasComponents.get(entity);
			world.getInputMultiplexer()
				 .removeProcessor(canvasComponent.getStage());
			canvasComponent.getStage()
						   .dispose();
		}
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof ResizeEvent) {
			ResizeEvent resizeEvent = (ResizeEvent) event;
			Dimension newSize = resizeEvent.getNewSize();
			if (getEntities() != null) {
				for (Entity entity : getEntities()) {
					CanvasComponent canvasComponent = canvasComponents.get(entity);
					canvasComponent.getStage()
								   .getViewport()
								   .update(newSize.width, newSize.height, true);
					canvasComponent.getStage()
								   .getViewport()
								   .setWorldSize(newSize.width, newSize.height);
				}
			}
		}
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		if (elementsToAdd.size > 0) {
			for (Entity toAdd : elementsToAdd) {
				EntityUtilities.relate(entity, toAdd);
			}
			elementsToAdd.clear();
		}
		CanvasComponent canvasComponent = canvasComponents.get(entity);
		canvasComponent.getStage().act(deltaTime);
	}
}
