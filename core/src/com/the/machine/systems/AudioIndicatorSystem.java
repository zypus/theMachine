package com.the.machine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.components.GrowthComponent;
import com.the.machine.events.AudioEvent;
import com.the.machine.framework.AbstractSystem;
import com.the.machine.framework.components.DelayedRemovalComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.ShapeRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.utility.BitBuilder;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/03/15
 */
public class AudioIndicatorSystem extends AbstractSystem implements EventListener {

	private final float SPEED_OF_SOUND = 10;

	@Override
	public void handleEvent(Event event) {
		if (event instanceof AudioEvent) {
			Entity audioIndicator = new Entity();
			audioIndicator.add(new TransformComponent().setPosition(((AudioEvent) event).getLocation()).setScale(0));
			audioIndicator.add(new ShapeRenderComponent().setSortingLayer("Physics 2d Debug").add((r) -> r.circle(0,0,2)));
			float distance = ((AudioEvent) event).getHearableDistance();
			float lifeTime = distance/SPEED_OF_SOUND;
			audioIndicator.add(new GrowthComponent().setGrowthRate(new Vector3(SPEED_OF_SOUND/2, SPEED_OF_SOUND/2, SPEED_OF_SOUND/2)));
			audioIndicator.add(new DelayedRemovalComponent().setDelay(lifeTime));
			audioIndicator.add(new LayerComponent(BitBuilder.none(32)
															.s(1)
															.get()));
			world.addEntity(audioIndicator);
		}
	}
}
