package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.systems.ActionSystem;

public class ResLeaf extends LeafTask<TreeContext>{
	
	private ActionSystem.Action action;
	private Object actionData;
	
	public ResLeaf(ActionSystem.Action action, Object actionData){
		this.action = action;
		this.actionData = actionData;
	}

	@Override
	/** A Returned Success Here Does Not Actually Imply Succesfull Action Completion */
	public void run(TreeContext context) {
		context.addResponse(action, actionData);
		super.success();
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		return this;
	}

}
