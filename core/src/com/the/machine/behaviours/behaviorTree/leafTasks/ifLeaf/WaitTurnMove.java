package com.the.machine.behaviours.behaviorTree.leafTasks.ifLeaf;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.behaviorTree.TreeContext;

public class WaitTurnMove extends LeafTask<TreeContext>{

	@Override
	public void run(TreeContext context) {
		if(!context.isCurrentlyTurning()){
			this.success();
		}
		else{
			System.out.println("Nope");
			this.fail();
		}
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}

}
