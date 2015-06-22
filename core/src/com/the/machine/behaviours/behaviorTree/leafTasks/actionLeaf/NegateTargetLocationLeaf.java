package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.behaviorTree.TreeContext;

public class NegateTargetLocationLeaf extends LeafTask<TreeContext>{

	@Override
	public void run(TreeContext context) {
		if(context.getTargetRelativeDirection()!=null){
			Vector2 vec = context.getTargetRelativeDirection();
			context.setTargetRelativeDirection(new Vector2(-vec.x, -vec.y));
			this.success();
		}
		else{
			this.fail();
		}
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}

}
