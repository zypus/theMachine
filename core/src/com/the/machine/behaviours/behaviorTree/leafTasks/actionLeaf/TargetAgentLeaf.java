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
		Iterator<WeakReference<Entity>> i = context.getBlackboard().getSeenAgentList().iterator();//context.getBehaviorContext().getAgents().iterator();
		while(i.hasNext()){
			WeakReference<Entity> e = i.next();
			if(!(e.get().getComponent(BehaviourComponent.class).getBehaviour() instanceof TreeBehavior)){
				TransformComponent t = e.get().getComponent(TransformComponent.class);
				Vector2 to = new Vector2(t.getX(), t.getY());
				Vector2 difference = to.cpy().sub(context.getBehaviorContext().getPlacebo().getPos());
				Vector2 finalDir = difference.rotate(-context.getBehaviorContext().getMoveDirection().angle());
				System.out.println(finalDir);
				context.setTargetRelativeDirection(finalDir);
				this.success();
				return;
			}
		}
		this.fail();
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}

}
