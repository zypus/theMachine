package com.the.machine.framework.utility.interfaceBuilder;

import com.badlogic.ashley.core.Entity;
import com.the.machine.framework.utility.InterfaceBuilder;
import com.the.machine.framework.utility.Interfacer;

import java.lang.reflect.Field;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/02/15
 */
public class AbstractComponentInterfaceBuilder implements InterfaceBuilder {
	@Override
	public Entity interfaceFor(Interfacer interfacer, Object component, Class<?> aClass, Object owner, Field field) {
		return null;
	}
}
