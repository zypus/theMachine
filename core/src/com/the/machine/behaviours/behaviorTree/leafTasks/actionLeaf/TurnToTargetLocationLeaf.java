package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import lombok.AllArgsConstructor;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.systems.ActionSystem;

@AllArgsConstructor
public class TurnToTargetLocationLeaf extends LeafTask<TreeContext>{
	
	private float speed;
	
	@Override
	public void run(TreeContext context) {
		if(context.getTargetLocation()!=null){
			Vector2 dir = ActionSystem.TurnData.convertGlobalTurn(context.getTargetLocation(), context.getBehaviorContext().getMoveDirection());//context.getBehaviorContext().getMoveDirection().cpy().rotate(context.getBehaviorContext().getMoveDirection().angle(context.getTargetLocation()) + context.getBehaviorContext().getMoveDirection().angle());
			dir = context.getTargetLocation();
			context.addResponse(ActionSystem.Action.TURN, new ActionSystem.TurnData(dir, speed));
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
