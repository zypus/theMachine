package com.the.machine.framework.utility;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.LabelComponent;
import com.the.machine.framework.components.canvasElements.SelectBoxComponent;
import com.the.machine.framework.components.canvasElements.TextFieldComponent;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/02/15
 */
public class EntityBuilder {

	private static ComponentMapper<SubEntityComponent> subs = ComponentMapper.getFor(SubEntityComponent.class);
	private static ComponentMapper<ParentComponent> parents = ComponentMapper.getFor(ParentComponent.class);

	public static Entity makeLabel(String text) {
		Entity label = new Entity();
		label.add(new TransformComponent());
		label.add(new DimensionComponent().setDimension(100, 20));
		label.add(new CanvasElementComponent());
		label.add(new LabelComponent().setText(text));
		return label;
	}

	public static Entity makeTextField(String text) {
		return makeTextField(text, 0);
	}

	public static Entity makeTextField(TextFieldComponent textFieldComponent) {
		Entity label = new Entity();
		label.add(new TransformComponent());
		label.add(new DimensionComponent());
		label.add(new CanvasElementComponent());
		label.add(textFieldComponent);
		return label;
	}

	public static Entity makeTextField(String text, int maxLength) {
		Entity label = new Entity();
		label.add(new TransformComponent());
		label.add(new DimensionComponent());
		label.add(new CanvasElementComponent());
		label.add(new TextFieldComponent().setText(text).setMaxLength(maxLength));
		return label;
	}

	public static Entity makeSelectBox(SelectBoxComponent selectBoxComponent) {
		Entity selectBox = new Entity();
		selectBox.add(new TransformComponent());
		selectBox.add(new DimensionComponent());
		selectBox.add(new CanvasElementComponent());
		selectBox.add(selectBoxComponent);
		return selectBox;
	}

	public static Entity relate(Entity parent, Entity child) {
		if (!subs.has(parent)) {
			parent.add(new SubEntityComponent());
		}
		SubEntityComponent subEntityComponent = subs.get(parent);
		int i = subEntityComponent.size();
		if (!parents.has(child)) {
			child.add(new ParentComponent(parent, i));
		} else {
			ParentComponent parentComponent = parents.get(child);
			Entity formerParent = parentComponent.getParent();
			SubEntityComponent formerSubs = subs.get(formerParent);
			formerSubs.remove(parentComponent.getIndex());
			parentComponent.setParent(parent);
			parentComponent.setIndex(i);
		}
		subEntityComponent.add(child);
		return child;
	}

}
