package com.the.machine.framework.systems.editor;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.IgnoredComponent;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.WorldComponent;
import com.the.machine.framework.components.WorldTreeComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.TreeComponent;
import com.the.machine.framework.components.canvasElements.TreeNodeComponent;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.utility.EntityBuilder;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/02/15
 */
public class WorldTreeSystem extends IteratingSystem {

	private transient ComponentMapper<WorldComponent>      worldComponents      = ComponentMapper.getFor(WorldComponent.class);
	private transient ComponentMapper<WorldTreeComponent> worldTreeComponents = ComponentMapper.getFor(WorldTreeComponent.class);
	private transient ComponentMapper<TreeComponent> trees = ComponentMapper.getFor(TreeComponent.class);
	private transient ComponentMapper<SubEntityComponent> subs = ComponentMapper.getFor(SubEntityComponent.class);
	private transient ComponentMapper<IgnoredComponent> ignored = ComponentMapper.getFor(IgnoredComponent.class);
	private transient ComponentMapper<NameComponent> names = ComponentMapper.getFor(NameComponent.class);

	private ImmutableArray<Entity> worlds;

	public WorldTreeSystem() {
		super(Family.all(TreeComponent.class, CanvasElementComponent.class, WorldTreeComponent.class)
					.get());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		worlds = engine.getEntitiesFor(Family.all(WorldComponent.class)
											 .get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		worlds = null;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		if (worlds != null && worlds.size() > 0) {
			WorldComponent worldComponent = worldComponents.get(worlds.get(0));
			World w = worldComponent.getWorld();
			ImmutableArray<Entity> roots = w.getEntitiesFor(Family.exclude(ParentComponent.class)
																  .get());
			if (!subs.has(entity)) {
				for (Entity root : roots) {
					digDown(root, entity);
				}
			}
		}
	}

	private void digDown(Entity parent, Entity treeNode) {
		if (!ignored.has(parent)) {
			Entity node = EntityBuilder.makeLabel((names.has(parent)) ? names.get(parent).getName() :"Entity " + parent.getId())
									   .add(new TreeNodeComponent())
									   .add(new IgnoredComponent());
			EntityBuilder.relate(treeNode, node);
			world.addEntity(node);
			if (subs.has(parent)) {
				for (Entity child : subs.get(parent)) {
					digDown(child, node);
				}
			}
		}
	}
}
