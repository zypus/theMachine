package com.the.machine.framework.systems.canvas;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.TreeComponent;
import com.the.machine.framework.components.canvasElements.TreeNodeComponent;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/02/15
 */
public class TreeSystem extends IteratingSystem {

	private transient ComponentMapper<TreeNodeComponent>      treeNodes     = ComponentMapper.getFor(TreeNodeComponent.class);
	private transient ComponentMapper<CanvasElementComponent> canvasElements = ComponentMapper.getFor(CanvasElementComponent.class);
	private transient ComponentMapper<SubEntityComponent>     subs           = ComponentMapper.getFor(SubEntityComponent.class);

	public TreeSystem() {
		super(Family.all(SubEntityComponent.class, CanvasElementComponent.class)
					.one(TreeComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		CanvasElementComponent elementComponent = canvasElements.get(entity);
		// handle children
		if (subs.has(entity) && elementComponent.getActor() != null) {
			SubEntityComponent children = subs.get(entity);
			Tree tree = (Tree) elementComponent.getUnwrappedActor();
			int index = 0;
			boolean invalidHierarchy = true;
			while (invalidHierarchy) {
				boolean removes = false;
				for (Entity child : children) {
					if (canvasElements.has(child) && treeNodes.has(child)) {
						CanvasElementComponent childElement = canvasElements.get(child);
						TreeNodeComponent nodeComponent = treeNodes.get(child);
						if (!nodeComponent.isAdded() && childElement.getActor() != null) {
							Tree.Node node;
							if (nodeComponent.getNode() == null) {
								node = new Tree.Node(childElement.getActor());
								nodeComponent.setNode(node);
							} else {
								node = nodeComponent.getNode();
							}
							node.setObject(new WeakReference<>(child));
							tree.insert(index, node);
							nodeComponent.setAdded(true);
						} else if (nodeComponent.isAdded() && childElement.getActor() != null && tree.getRootNodes()
																									 .get(index) != nodeComponent.getNode()) {
							tree.remove(nodeComponent.getNode());
							nodeComponent.setAdded(false);
							removes = true;
						}
						index++;
					}
				}
				if (!removes) {
					invalidHierarchy = false;
				}
			}
		}
	}

}
