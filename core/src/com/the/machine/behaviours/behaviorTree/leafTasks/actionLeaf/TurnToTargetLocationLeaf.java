package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import lombok.AllArgsConstructor;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.systems.ActionSystem;

@AllArgsConstructor
public class TurnToTargetLocationLeaf extends LeafTask<TreeContext>{
	
	private float speed;
	
	@Override
	public void run(TreeContext context) {
		if(object.getTargetLocation()!=null){
			context.addResponse(ActionSystem.Action.TURN, new ActionSystem.TurnData(context.getBehaviorContext().getMoveDirection().cpy().rotate(context.getBehaviorContext().getMoveDirection().angle(context.getTargetLocation())-90), speed));
			context.setTargetLocation(null);
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
