package com.the.machine.framework.utility;

import com.badlogic.ashley.core.Entity;

import java.lang.reflect.Field;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/02/15
 */
public interface InterfaceBuilder<T> {

	Entity interfaceFor(Interfacer interfacer, T object, Class<?> aClass, Object owner, Field field);

}
