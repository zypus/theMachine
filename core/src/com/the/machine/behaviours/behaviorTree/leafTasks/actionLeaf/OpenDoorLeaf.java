package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.components.AreaComponent.AreaType;
import com.the.machine.components.DiscreteMapComponent.MapCell;
import com.the.machine.systems.ActionSystem;

public class OpenDoorLeaf extends LeafTask<TreeContext>{
	
	private boolean running = false;

	@Override
	public void run(TreeContext context) {
		if(context.getBehaviorContext().getEnvironment().equals(AreaType.DOOR_CLOSED)){
			context.addResponse(ActionSystem.Action.DOOR_OPEN, "Dummy");
			this.running = true;
			this.running();
		}
		else{
			this.success();
			if(running){
				running = false;
				context.addResponse(ActionSystem.Action.MOVE, new ActionSystem.MoveData(50));
			}
		}
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}

}
