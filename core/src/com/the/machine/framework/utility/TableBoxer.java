package com.the.machine.framework.utility;

import com.badlogic.ashley.core.Entity;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.components.canvasElements.TableComponent;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/02/15
 */
public class TableBoxer implements Boxer {
	@Override
	public Entity box(Entity ... entities) {
		Entity box = new Entity();
		box.add(new TableComponent().setFillParent(true).setHorizontalAlignment(Enums.HorizontalAlignment.RIGHT).setVerticalAlignment(Enums.VerticalAlignment.BOTTOM));
		box.add(new CanvasElementComponent());
		box.add(new TransformComponent());
		box.add(new DimensionComponent());
		SubEntityComponent subEntityComponent = new SubEntityComponent();
		for (int i = 0; i < entities.length; i++) {
			Entity entity = entities[i];
			entity.add(new TableCellComponent().setHorizontalAlignment(Enums.HorizontalAlignment.LEFT).setFillX(1)
											   .setRowEnd(true));
			entity.add(new ParentComponent(box, i));
			subEntityComponent.add(entity);
		}
		box.add(subEntityComponent);
		return box;
	}
}
