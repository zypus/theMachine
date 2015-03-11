package com.the.machine.framework.systems.editor;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.InspectorComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.SelectedComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.utility.Interfacer;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 01/03/15
 */
public class InspectorSystem extends IteratingSystem {

	transient private ComponentMapper<SelectedComponent> selected = ComponentMapper.getFor(SelectedComponent.class);
	transient private ComponentMapper<InspectorComponent> inspectors = ComponentMapper.getFor(InspectorComponent.class);
	transient private ComponentMapper<SubEntityComponent> subs    = ComponentMapper.getFor(SubEntityComponent.class);
	transient private ComponentMapper<ParentComponent>    parents = ComponentMapper.getFor(ParentComponent.class);

	transient Interfacer interfacer = new Interfacer();

	transient ImmutableArray<Entity> selectedEntities;

	public InspectorSystem() {
		super(Family.all(InspectorComponent.class)
					.get());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		selectedEntities = engine.getEntitiesFor(Family.all(SelectedComponent.class)
													   .get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		selectedEntities = null;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		Entity first = selectedEntities.first();
		SelectedComponent selectedComponent = selected.get(first);

		InspectorComponent inspectorComponent = inspectors.get(entity);
		WeakReference<Entity> currentSelection = inspectorComponent.getSubject();

		if (selectedComponent.getSelection() != currentSelection) {
			currentSelection = selectedComponent.getSelection();
			inspectorComponent.setSubject(currentSelection);
			Entity selection = currentSelection.get();
			if (selection != null) {
				Entity inter = interfacer.interfaceFor(selection);
				inter.add(new TableCellComponent());
				if (subs.has(entity)) {
					for (Entity e : subs.get(entity)) {
						world.removeEntity(e);
					}
				}
				SubEntityComponent subEntityComponent = new SubEntityComponent();
				subEntityComponent.add(inter);
				entity.add(subEntityComponent);
				inter.add(new ParentComponent(new WeakReference<>(entity), 0));
				world.addEntity(inter, true);
			}
		}
	}
}
