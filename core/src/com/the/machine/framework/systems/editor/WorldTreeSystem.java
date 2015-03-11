package com.the.machine.framework.systems.editor;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.IgnoredComponent;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.ReferenceComponent;
import com.the.machine.framework.components.SelectedComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.WorldComponent;
import com.the.machine.framework.components.WorldTreeComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.TreeComponent;
import com.the.machine.framework.components.canvasElements.TreeNodeComponent;
import com.the.machine.framework.utility.EntityUtilities;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/02/15
 */
public class WorldTreeSystem extends IteratingSystem {

	private transient ComponentMapper<WorldComponent>      worldComponents      = ComponentMapper.getFor(WorldComponent.class);
	private transient ComponentMapper<WorldTreeComponent> worldTreeComponents = ComponentMapper.getFor(WorldTreeComponent.class);
	private transient ComponentMapper<TreeComponent>       trees   = ComponentMapper.getFor(TreeComponent.class);
	private transient ComponentMapper<CanvasElementComponent>       canvasElements   = ComponentMapper.getFor(CanvasElementComponent.class);
	private transient ComponentMapper<SubEntityComponent>  subs    = ComponentMapper.getFor(SubEntityComponent.class);
	private transient ComponentMapper<IgnoredComponent>    ignored = ComponentMapper.getFor(IgnoredComponent.class);
	private transient ComponentMapper<NameComponent>       names   = ComponentMapper.getFor(NameComponent.class);
	private transient ComponentMapper<SelectedComponent>       selected   = ComponentMapper.getFor(SelectedComponent.class);
	private transient ComponentMapper<ReferenceComponent>       referenced   = ComponentMapper.getFor(ReferenceComponent.class);

	transient private ImmutableArray<Entity> worlds;

	transient Entity selector;

	public WorldTreeSystem() {
		super(Family.all(TreeComponent.class, CanvasElementComponent.class, WorldTreeComponent.class)
					.get());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		worlds = engine.getEntitiesFor(Family.all(WorldComponent.class)
											 .get());
		selector = new Entity();
		selector.add(new SelectedComponent());
		selector.add(new IgnoredComponent());
		world.addEntity(selector);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		worlds = null;
		world.removeEntity(selector);
		selector = null;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		if (worlds != null && worlds.size() > 0) {
			Entity firstWorld = worlds.first();
			if (!subs.has(entity) || subs.get(entity)
										 .size() == 0) {
				if (firstWorld != null) {
					Entity node = EntityUtilities.makeLabel((names.has(firstWorld))
															? names.get(firstWorld)
																   .getName()
															: "Entity " + firstWorld.getId())
												 .add(new TreeNodeComponent())
							.add(new ReferenceComponent().setReference(new WeakReference<>(firstWorld)))
												 .add(new IgnoredComponent());
					EntityUtilities.relate(entity, node);
					world.addEntity(node);
					digDown(firstWorld, node);
				}
			} else {
				CanvasElementComponent elementComponent = canvasElements.get(entity);
				Tree tree = (Tree) elementComponent.getUnwrappedActor();
				Tree.Node first = tree.getSelection()
									  .first();
				if (first != null) {
					Entity selectedEntity = ((WeakReference<Entity>) first.getObject()).get();
					if (selectedEntity != null) {
						Entity reference = (Entity) referenced.get(selectedEntity)
													 .getReference()
													 .get();
						if (reference != null) {
							SelectedComponent selectedComponent = selected.get(selector);
							if (selectedComponent.getSelection() == null || reference != selectedComponent.getSelection()
																										  .get()) {
								selectedComponent.setSelection(new WeakReference<>(reference));
							}
						}
					}
				}
			}
		}
	}

	private void digDown(Entity parent, Entity treeNode) {
		if (!ignored.has(parent)) {
			if (subs.has(parent)) {
				for (Entity child : subs.get(parent)) {
					if (!ignored.has(child)) {
						Entity node = EntityUtilities.makeLabel((names.has(child))
																? names.get(child)
																	   .getName()
																: "Entity " + child.getId())
													 .add(new TreeNodeComponent())
													.add(new ReferenceComponent().setReference(new WeakReference<>(child)))
													 .add(new IgnoredComponent());
						EntityUtilities.relate(treeNode, node);
						world.addEntity(node);
						digDown(child, node);
					}
				}
			}
		}
	}
}
