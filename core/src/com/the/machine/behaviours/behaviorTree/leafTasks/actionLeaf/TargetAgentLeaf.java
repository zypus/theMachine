package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import java.lang.ref.WeakReference;
import java.util.Iterator;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.behaviorTree.TreeBehavior;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.framework.components.TransformComponent;

public class TargetAgentLeaf extends LeafTask<TreeContext>{

	@Override
	public void run(TreeContext context) {
		System.out.println("Hello");
		Iterator<WeakReference<Entity>> i = context.getBehaviorContext().getAgents().iterator();
		while(i.hasNext()){
			WeakReference<Entity> e = i.next();
			if(!(e.get().getComponent(BehaviourComponent.class).getBehaviour() instanceof TreeBehavior)){
				System.out.println("Found him");
				TransformComponent t = e.get().getComponent(TransformComponent.class);
				Vector2 to = new Vector2(t.get2DPosition().x, t.get2DPosition().y);
				context.setDestination(to);
				this.success();
				return;
			}
		}
		this.success();
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}

}
