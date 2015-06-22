package com.the.machine.behaviours.behaviorTree.leafTasks.ifLeaf;

import java.lang.ref.WeakReference;
import java.util.List;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.behaviorTree.TreeContext;

public class HearingLeaf extends LeafTask<TreeContext>{

	@Override
	public void run(TreeContext context) {
		List<Vector2> list = context.getBehaviorContext().getSoundDirections();
		if(!list.isEmpty()){
			context.setTargetRelativeDirection(list.get(0));
			super.success();
		}
		else{
			super.fail();
		}
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		return null;
	}

}
