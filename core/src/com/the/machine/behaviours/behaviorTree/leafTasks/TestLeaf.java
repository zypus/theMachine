package com.the.machine.behaviours.behaviorTree.leafTasks;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.behaviorTree.TreeContext;

public class TestLeaf extends LeafTask<TreeContext>{

	@Override
	public void run(TreeContext object) {
		System.out.println("hi");
		this.running();
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
