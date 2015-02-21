package com.the.machine.framework.utility;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.LabelComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.components.canvasElements.TableComponent;
import com.the.machine.framework.utility.interfaceBuilder.EnumInterfaceBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/02/15
 */
public class DefaultInterfaceBuilder implements InterfaceBuilder {

	InterfaceBuilder enumBuilder = new EnumInterfaceBuilder();

	@Override
	public Entity interfaceFor(Interfacer interfacer, Object object, Class<?> aClass, Object owner, Field f) {
		Field[] fields = aClass.getDeclaredFields();
		Entity comp = new Entity();
		comp.add(new TableComponent().setHorizontalAlignment(Enums.HorizontalAlignment.LEFT));
		comp.add(new CanvasElementComponent());
		comp.add(new TransformComponent());
		comp.add(new DimensionComponent());
		for (Field field : fields) {
			if (!Modifier.isTransient(field.getModifiers())) {
				Entity part;
				EntityBuilder.relate(comp, EntityBuilder.makeLabel(field.getName())
														.add(new TableCellComponent().setHorizontalAlignment(Enums.HorizontalAlignment.LEFT)
																					 .setSpaceRight(new Value.Fixed(10))));
				Class<?> fieldClass = field.getType();
				if (interfacer.getInterfaceBuilders()
							  .containsKey(fieldClass)) {
					field.setAccessible(true);
					try {
						part = interfacer.getInterfaceBuilders()
										 .get(fieldClass)
										 .interfaceFor(interfacer, field.get(object), fieldClass, object, field);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						part = errorEntity(field);
					}
				} else if (fieldClass.isEnum()) {
					field.setAccessible(true);
					try {
						part = enumBuilder.interfaceFor(interfacer, field.get(object), fieldClass, object, field);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						part = errorEntity(field);
					}
				} else {
					part = missingEntity(field);
				}
				part.add(new TableCellComponent().setFillX(1).setRowEnd(true));
				EntityBuilder.relate(comp, part);
			}
		}
		return comp;
	}

	private Entity missingEntity(Field field) {
		Entity error = new Entity();
		error.add(new LabelComponent().setText("No interface for " + field.getName()+":"+field.getType().getSimpleName())
									  .setColor(Color.RED).setAlignment(Align.left));
		error.add(new CanvasElementComponent());
		error.add(new TransformComponent());
		error.add(new DimensionComponent());
		return error;
	}

	private Entity errorEntity(Field field) {
		Entity error = new Entity();
		error.add(new LabelComponent().setText("Error with " + field.getName() + ":" + field.getType()
																							 .getSimpleName())
									  .setColor(Color.RED));
		error.add(new CanvasElementComponent());
		error.add(new TransformComponent());
		error.add(new DimensionComponent());
		return error;
	}

}
