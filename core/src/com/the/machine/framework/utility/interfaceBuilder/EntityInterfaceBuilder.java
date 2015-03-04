package com.the.machine.framework.utility.interfaceBuilder;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.components.canvasElements.TableComponent;
import com.the.machine.framework.utility.InterfaceBuilder;
import com.the.machine.framework.utility.Interfacer;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 28/02/15
 */
public class EntityInterfaceBuilder implements InterfaceBuilder<Entity> {

	@Override
	public Entity interfaceFor(Interfacer interfacer, Entity entity, Class<?> aClass, Object owner, Field field) {
		Entity entityInterface = new Entity();
		entityInterface.add(new TableComponent());
		entityInterface.add(new CanvasElementComponent());
		entityInterface.add(new TransformComponent());
		entityInterface.add(new DimensionComponent());
		SubEntityComponent subs = new SubEntityComponent();
		int index = 0;
		for (Component component : entity.getComponents()) {
			if (!(component instanceof ParentComponent) && !(component instanceof SubEntityComponent)) {
				Entity componentInterface = interfacer.interfaceFor(component);
				componentInterface.add(new TableCellComponent().setExpandX(1)
															   .setFillX(1)
															   .setRowEnd(true));
				subs.add(componentInterface);
				componentInterface.add(new ParentComponent(new WeakReference<>(entityInterface), index));
				index++;
			}
		}
		entityInterface.add(subs);
		return entityInterface;
	}
}
