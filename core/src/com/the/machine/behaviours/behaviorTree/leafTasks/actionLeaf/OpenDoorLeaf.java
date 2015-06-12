package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.components.AreaComponent.AreaType;
import com.the.machine.components.DiscreteMapComponent.MapCell;
import com.the.machine.systems.ActionSystem;

public class OpenDoorLeaf extends LeafTask<TreeContext>{

	@Override
	public void run(TreeContext context) {
		if(context.getBehaviorContext().getEnvironment().equals(AreaType.DOOR_CLOSED)){
			context.addResponse(ActionSystem.Action.DOOR_OPEN, "Dummy");
			this.running();
		}
		else{
			this.success();
		}
		System.out.println(context.getId()+" "+context.getTargetRelativeDirection());
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}

}
