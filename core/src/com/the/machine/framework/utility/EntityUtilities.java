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
import lombok.Getter;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

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

	private static Map<Entity, SubEntityComponent> relationMemory = new HashMap<>();
	@Getter private static Map<Entity, ParentComponent> parentMemory = new HashMap<>();

	public static Entity makeLabel(String text) {
		return makeLabel(text, 100, 20);
	}

	public static Entity makeLabel(String text, int width, int height) {
		Entity label = new Entity();
		label.add(new TransformComponent());
		label.add(new DimensionComponent().setDimension(width, height));
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
		SubEntityComponent subEntityComponent;
		if (!relationMemory.containsKey(parent) && !subs.has(parent)) {
			subEntityComponent = new SubEntityComponent();
			parent.add(subEntityComponent);
			relationMemory.put(parent, subEntityComponent);
		} else if (subs.has(parent)) {
			subEntityComponent = subs.get(parent);
		} else {
			subEntityComponent = relationMemory.get(parent);
		}
		int i = subEntityComponent.size();
		if (!parentMemory.containsKey(child) && !parents.has(child)) {
			ParentComponent parentComponent = new ParentComponent(new WeakReference<>(parent), i);
			child.add(parentComponent);
			parentMemory.put(child, parentComponent);
		} else {
			ParentComponent parentComponent = parents.get(child);
			if (parentComponent == null) {
				parentComponent = parentMemory.get(child);
			}
			Entity formerParent = parentComponent.getParent().get();
			if (formerParent != null && formerParent != parent) {
				SubEntityComponent formerSubs = subs.get(formerParent);
				if (formerSubs != null) {
					formerSubs.remove(child);
				} else {
					formerSubs = relationMemory.get(formerParent);
					if (formerSubs != null) {
						formerSubs.remove(child);
					}
				}
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
			if (subs.get(parent)
					.size() == 0) {
				parent.remove(SubEntityComponent.class);
				relationMemory.remove(parent);
			}
		}
		if (parents.has(child)) {
			parentMemory.remove(child);
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
