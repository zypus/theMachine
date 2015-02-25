package com.the.machine.framework.serialization;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 22/02/15
 */
public class EntitySerializer extends Serializer<Entity> {
	@Override
	public void write(Kryo kryo, Output output, Entity object) {
		kryo.writeObject(output, object.getComponents());
	}

	@Override
	public Entity read(Kryo kryo, Input input, Class<Entity> type) {
		Entity entity = new Entity();
		kryo.reference(entity);
		ImmutableArray array = kryo.readObject(input, ImmutableArray.class);
		for (Object o : array) {
			entity.add((Component) o);
		}
		return entity;
	}


}
