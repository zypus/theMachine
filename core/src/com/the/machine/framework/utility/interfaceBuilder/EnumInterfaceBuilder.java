package com.the.machine.framework.utility.interfaceBuilder;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.the.machine.framework.components.canvasElements.SelectBoxComponent;
import com.the.machine.framework.utility.EntityUtilities;
import com.the.machine.framework.utility.InterfaceBuilder;
import com.the.machine.framework.utility.Interfacer;

import java.lang.reflect.Field;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/02/15
 */
public class EnumInterfaceBuilder implements InterfaceBuilder {
	@Override
	public Entity interfaceFor(Interfacer interfacer, Object object, Class<?> aClass, Object owner, Field field) {
		Object[] constants = aClass.getEnumConstants();
		Entity selectBox = EntityUtilities.makeSelectBox(new SelectBoxComponent().setItems(new Array(constants)));
		return selectBox;
	}
}
