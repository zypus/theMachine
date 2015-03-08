package com.the.machine.framework.serialization;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.SubEntityComponent;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 22/02/15
 */
public class EntitySerializer extends Serializer<Entity> {
	@Override
	public void write(Kryo kryo, Output output, Entity object) {
		ImmutableArray<Component> components = object.getComponents();
		Array<Component> newComponents = new Array<>(components.size());
		for (Component component : components) {
			if (!(component instanceof ParentComponent)) {
				newComponents.add(component);
			}
		}
		kryo.writeObject(output, newComponents);
	}

	@Override
	public Entity read(Kryo kryo, Input input, Class<Entity> type) {
		Entity entity = new Entity();
		kryo.reference(entity);
		Array array = kryo.readObject(input, Array.class);
		for (Object o : array) {
			Component component = (Component) o;
			if (component instanceof SubEntityComponent) {
				SubEntityComponent subEntityComponent = (SubEntityComponent) component;
				for (Entity subEntity : subEntityComponent) {
					subEntity.add(new ParentComponent().setParent(new WeakReference<>(entity)));
				}
			}
			entity.add(component);
		}
		return entity;
	}


}
