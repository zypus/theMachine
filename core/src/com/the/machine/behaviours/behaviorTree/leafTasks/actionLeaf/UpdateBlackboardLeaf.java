package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.behaviorTree.Blackboard;
import com.the.machine.behaviours.behaviorTree.TreeBehavior;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.framework.components.TransformComponent;

public class UpdateBlackboardLeaf extends LeafTask<TreeContext>{

	@Override
	public void run(TreeContext context) {
		addCommonVision(context);
	}
	
	/** Adds All Agents That Are Seen To The Blackboard */
	private void addCommonVision(TreeContext context){
		Blackboard b = context.getBlackboard();
		List<WeakReference<Entity>> list = b.getSeenAgentList();
		Iterator<WeakReference<Entity>> i = context.getBehaviorContext().getAgents().iterator();
		while(i.hasNext()){
			boolean alreadyIn = false;
			WeakReference<Entity> e = i.next();
			for(WeakReference<Entity> le : list){
				if(e.equals(le)){
					alreadyIn = true;
				}
			}
			if(!alreadyIn){
				list.add(e);
			}
		}
		this.success();
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		return null;
	}

}
