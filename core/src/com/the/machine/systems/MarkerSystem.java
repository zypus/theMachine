package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.the.machine.components.MarkerComponent;
import com.the.machine.events.MarkerEvent;
import com.the.machine.events.ResetEvent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.utility.BitBuilder;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/03/15
 */
public class MarkerSystem extends IteratingSystem
		implements EventListener {

	transient private ComponentMapper<MarkerComponent> markerMap = ComponentMapper.getFor(MarkerComponent.class);

	private static final Color[] COLORS = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.PURPLE };

	transient private ImmutableArray<Entity> markers;

	public MarkerSystem() {
		super(Family.all(MarkerComponent.class).get());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		markers = world.getEntitiesFor(Family.all(MarkerComponent.class)
											 .get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		markers = null;
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof MarkerEvent) {
			Entity marker = new Entity();
			MarkerEvent markerEvent = (MarkerEvent) event;
			marker.add(new TransformComponent().setPosition(markerEvent.getLocation()));
			marker.add(new MarkerComponent().setGuardMarker(markerEvent.isGuardMarker())
											.setMarkerNumber(markerEvent.getMarkerNumber())
											.setDecayRate(markerEvent.getDecayRate())
											.setStrength(1));
			SpriteRenderComponent spriteRenderComponent = new SpriteRenderComponent();
			if (markerEvent.isGuardMarker()) {
				spriteRenderComponent.setTextureRegion(Asset.fetch("marker_guard", TextureRegion.class));
			} else {
				spriteRenderComponent.setTextureRegion(Asset.fetch("marker_intruder", TextureRegion.class));
			}
			spriteRenderComponent.setTint(COLORS[markerEvent.getMarkerNumber()]);
			spriteRenderComponent.setSortingLayer("Default");
			marker.add(spriteRenderComponent);
			marker.add(new LayerComponent(BitBuilder.none(32)
													  .s(1)
													  .get()));
			world.addEntity(marker);
		} else if (event instanceof ResetEvent) {
			Entity[] array = new Entity[markers.size()];
			int index = 0;
			for (Entity element : markers) {
				array[index] = element;
				index++;
			}
			for (Entity entity : array) {
				world.removeEntity(entity);
			}
		}
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		MarkerComponent markerComponent = markerMap.get(entity);
		float strength = markerComponent.getStrength();
		float rate = markerComponent.getDecayRate();
		float newStrength = strength - deltaTime * rate * strength;
		if (newStrength <= 0) {
			world.removeEntity(entity);
		} else {
			markerComponent.setStrength(newStrength);
		}
	}
}
