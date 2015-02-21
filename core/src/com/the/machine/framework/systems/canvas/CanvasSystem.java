package com.the.machine.framework.systems.canvas;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.CanvasComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.basic.ResizeEvent;

import java.awt.Dimension;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/02/15
 */
public class CanvasSystem extends IteratingSystem
		implements EntityListener, EventListener {

	private ComponentMapper<CanvasComponent> canvasComponents = ComponentMapper.getFor(CanvasComponent.class);
	private ComponentMapper<CanvasElementComponent> canvasElements = ComponentMapper.getFor(CanvasElementComponent.class);

	public CanvasSystem() {
		super(Family.all(CanvasComponent.class, CanvasElementComponent.class)
					.get(), 0);
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(family, this);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
	}

	@Override
	public void entityAdded(Entity entity) {
		CanvasComponent canvasComponent = canvasComponents.get(entity);
		canvasComponent.setBatch(new SpriteBatch());
		Stage stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		canvasComponent.setStage(stage);

		CanvasElementComponent elementComponent = canvasElements.get(entity);
		elementComponent.setActor(stage.getRoot());
		elementComponent.setGroup(true);
		elementComponent.setAdded(true);
	}

	@Override
	public void entityRemoved(Entity entity) {
		CanvasComponent canvasComponent = canvasComponents.get(entity);
		canvasComponent.getStage()
					   .dispose();
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof ResizeEvent) {
			ResizeEvent resizeEvent = (ResizeEvent) event;
			Dimension newSize = resizeEvent.getNewSize();
			for (Entity entity : getEntities()) {
				CanvasComponent canvasComponent = canvasComponents.get(entity);
				canvasComponent.getStage().getViewport().update(newSize.width, newSize.height, true);
				canvasComponent.getStage().getViewport().setWorldSize(newSize.width, newSize.height);
			}
		}
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		CanvasComponent canvasComponent = canvasComponents.get(entity);
		canvasComponent.getStage().act(deltaTime);
	}
}
