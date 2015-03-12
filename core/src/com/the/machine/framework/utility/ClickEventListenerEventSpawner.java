package com.the.machine.framework.utility;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.events.Event;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 07/03/15
 */
public class ClickEventListenerEventSpawner extends ClickListener {

	transient private World                  world;
	private                   Event event;

	public ClickEventListenerEventSpawner(World world, Event event) {
		super();
		this.world = world;
		this.event = event;
	}

	@Override
	public void clicked(InputEvent event, float x, float y) {
		world.dispatchEvent(this.event);
		event.handle();
	}

	public static class ClickEventListenerEventSpawnerSerializer
			extends Serializer<ClickEventListenerEventSpawner> {

		private World world;

		public ClickEventListenerEventSpawnerSerializer(World world) {
			this.world = world;
		}

		@Override
		public void write(Kryo kryo, Output output, ClickEventListenerEventSpawner object) {
			kryo.getDefaultSerializer(Object.class).write(kryo, output, object);
		}

		@Override
		public ClickEventListenerEventSpawner read(Kryo kryo, Input input, Class<ClickEventListenerEventSpawner> type) {
			ClickEventListenerEventSpawner listener = (ClickEventListenerEventSpawner)kryo.getDefaultSerializer(Object.class)
							  .read(kryo, input, type);
			listener.world = world;
			return listener;
		}
	}

}
