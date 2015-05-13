package com.the.machine.behaviours.behaviorTree.leafTasks;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.systems.ActionSystem;

public class TestLeaf extends LeafTask<TreeContext>{

	@Override
	public void run(TreeContext context) {
		context.addResponse(
				ActionSystem.Action.TURN, 
				new ActionSystem.TurnData(3, 6));
		this.running();
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
