package com.the.machine.framework.utility.interfaceBuilder;

import com.badlogic.ashley.core.Entity;
import com.the.machine.framework.components.ObservableComponent;
import com.the.machine.framework.utility.InterfaceBuilder;
import com.the.machine.framework.utility.Interfacer;

import java.lang.reflect.Field;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/02/15
 */
public class OwnableComponentInterfaceBuilder implements InterfaceBuilder<ObservableComponent> {
	@Override
	public Entity interfaceFor(Interfacer interfacer, ObservableComponent component, Class<?> aClass, Object owner, Field field) {
		return null;
	}
}
