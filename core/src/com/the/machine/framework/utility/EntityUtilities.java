package com.the.machine.framework.utility;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.DisabledComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.LabelComponent;
import com.the.machine.framework.components.canvasElements.SelectBoxComponent;
import com.the.machine.framework.components.canvasElements.TextFieldComponent;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/02/15
 */
public class EntityUtilities {

	private static ComponentMapper<SubEntityComponent> subs = ComponentMapper.getFor(SubEntityComponent.class);
	private static ComponentMapper<ParentComponent> parents = ComponentMapper.getFor(ParentComponent.class);
	private static ComponentMapper<DisabledComponent> disabled = ComponentMapper.getFor(DisabledComponent.class);
	private static ComponentMapper<TransformComponent>   transforms  = ComponentMapper.getFor(TransformComponent.class);

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
		label.add(new TextFieldComponent().setText(text)
										  .setMaxLength(maxLength));
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
			child.add(new ParentComponent(new WeakReference<>(parent), i));
		} else {
			ParentComponent parentComponent = parents.get(child);
			Entity formerParent = parentComponent.getParent().get();
			if (formerParent != null) {
				SubEntityComponent formerSubs = subs.get(formerParent);
				formerSubs.remove(parentComponent.getIndex());
			}
			parentComponent.setParent(new WeakReference<>(parent));
			parentComponent.setIndex(i);
		}
		subEntityComponent.add(child);
		return child;
	}

	public static void derelate(Entity parent, Entity child) {
		if (subs.has(parent)) {
			subs.get(parent)
				.remove(child);
		}
		if (parents.has(child)) {
			child.remove(ParentComponent.class);
		}
	}

	public static boolean isEntityEnabled(Entity entity) {
		if (entity == null) {
			return true;
		}
		if (disabled.has(entity)) {
			return false;
		} else {
			return !parents.has(entity) || isEntityEnabled(parents.get(entity)
																  .getParent()
																  .get());
		}
	}

	public static TransformComponent computeAbsoluteTransform(Entity entity) {
		Vector3 position = new Vector3();
		Quaternion rotation = new Quaternion();
		Vector3 scale = new Vector3(1, 1, 1);
		if (entity != null && transforms.has(entity)) {
			if (parents.has(entity)) {
				TransformComponent parentTransform = computeAbsoluteTransform(parents.get(entity)
																					 .getParent().get());
				position = parentTransform.getPosition()
										  .cpy();
				rotation = parentTransform.getRotation()
										  .cpy();
				scale = parentTransform.getScale()
									   .cpy();
			}
			TransformComponent transformComponent = transforms.get(entity);
			position.add(transformComponent.getPosition()
										   .cpy()
										   .scl(scale.x, scale.y, scale.z)
										   .mul(rotation));
			rotation.mul(transformComponent.getRotation());
			scale.x *= transformComponent.getXScale();
			scale.y *= transformComponent.getYScale();
			scale.z *= transformComponent.getZScale();
		}
		return new TransformComponent(position, rotation, scale);
	}


}
