package com.the.machine.behaviours.behaviorTree.leafTasks.ifLeaf;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.ResLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.TurnToTargetLocationLeaf;
import com.the.machine.systems.ActionSystem;

public class WaitTurnMove extends LeafTask<TreeContext>{

	@Override
	public void run(TreeContext context) {
		context.addResponse(ActionSystem.Action.TURN, new ActionSystem.TurnData(context.getTargetLocation(), 20));
		System.out.println(context.getBehaviorContext().getCurrentTurningSpeed());
		if(context.isCurrentlyTurning()){
			this.running();
		}
		else{
			this.success();
		}
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}

}
