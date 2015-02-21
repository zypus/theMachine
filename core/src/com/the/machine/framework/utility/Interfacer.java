package com.the.machine.framework.utility;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.framework.components.AbstractComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.ObservableComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.LabelComponent;
import com.the.machine.framework.utility.interfaceBuilder.AbstractComponentInterfaceBuilder;
import com.the.machine.framework.utility.interfaceBuilder.IntegerInterfaceBuilder;
import com.the.machine.framework.utility.interfaceBuilder.OwnableComponentInterfaceBuilder;
import com.the.machine.framework.utility.interfaceBuilder.QuaternionInterfaceBuilder;
import com.the.machine.framework.utility.interfaceBuilder.Vector3InterfaceBuilder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 19/02/15
 */
public class Interfacer {

	private InterfaceBuilder defaultBuilder = new DefaultInterfaceBuilder();
	private Boxer boxer = new TableBoxer();
	@Getter private Map<Class<?>, InterfaceBuilder>      interfaceBuilders = new HashMap<>();

	private Component component;

	public Interfacer() {
		interfaceBuilders.put(AbstractComponent.class, new AbstractComponentInterfaceBuilder());
		interfaceBuilders.put(ObservableComponent.class, new OwnableComponentInterfaceBuilder());
		interfaceBuilders.put(Vector3.class, new Vector3InterfaceBuilder());
		interfaceBuilders.put(Quaternion.class, new QuaternionInterfaceBuilder());
		interfaceBuilders.put(Integer.class, new IntegerInterfaceBuilder());
	}

	public Entity interfaceFor(Object object) {
		Class<? extends Object> aClass = object.getClass();
		List<Entity> parts = new ArrayList<>();
		if (interfaceBuilders.containsKey(aClass)) {
			return boxer.box(interfaceBuilders.get(aClass)
									.interfaceFor(this, object, aClass, object, null));
		} else {
			parts.add(classTitle(aClass));
			List<Class<?>> classList = unfold(aClass);
			for (Class<?> c : classList) {
				Entity part;
				if (interfaceBuilders.containsKey(c)) {
					part = interfaceBuilders.get(c)
											.interfaceFor(this, object, c, object, null);
				} else {
					part = defaultBuilder.interfaceFor(this, object, c, object, null);
				}
				if (part != null) {
					parts.add(part);
				}
			}
			return boxer.box(parts.toArray(new Entity[parts.size()]));
		}
	}

	private Entity classTitle(Class<?> aClass) {
		Entity title = new Entity();
		title.add(new LabelComponent().setText(aClass.getSimpleName()));
		title.add(new CanvasElementComponent());
		title.add(new TransformComponent());
		title.add(new DimensionComponent());
		return title;
	}

	private List<Class<?>> unfold(Class<?> aClass) {
		if (aClass == Object.class) {
			return new ArrayList<>();
		} else {
			Class<?> superclass = aClass.getSuperclass();
			List<Class<?>> classList = unfold(superclass);
			classList.add(aClass);
			return classList;
		}
	}

}
