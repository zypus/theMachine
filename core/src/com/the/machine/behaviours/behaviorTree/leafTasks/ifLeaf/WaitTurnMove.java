package com.the.machine.behaviours.behaviorTree.leafTasks.ifLeaf;

import lombok.AllArgsConstructor;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.ResLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.TurnToTargetLocationLeaf;
import com.the.machine.systems.ActionSystem;

@AllArgsConstructor
public class WaitTurnMove extends LeafTask<TreeContext>{
	
	private float turnSpeed;

	@Override
	public void run(TreeContext context) {
		if(context.getTargetRelativeDirection()==null){
			this.fail();
			return;
		}
		context.addResponse(ActionSystem.Action.TURN, new ActionSystem.TurnData(context.getTargetRelativeDirection(), turnSpeed));
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
