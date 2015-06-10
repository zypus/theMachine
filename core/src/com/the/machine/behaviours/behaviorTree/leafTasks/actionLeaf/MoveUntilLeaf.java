package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.systems.ActionSystem;

public class MoveUntilLeaf extends LeafTask<TreeContext>{
	
	private float speed;
	
	private Vector2 target = null;
	private int timer = 1;
	private int time = 0;
	
	public MoveUntilLeaf(float speed){
		this.speed = speed;
	}
	
	@Override
	public void run(TreeContext context) {
		if(target==null){
			this.time = 0;
			target = context.getTargetRelativeDirection();
			context.addResponse(ActionSystem.Action.MOVE, new ActionSystem.MoveData(speed));
			this.running();
		}
		else if(time>=timer){
			this.success();
			target = null;
			context.addResponse(ActionSystem.Action.MOVE, new ActionSystem.MoveData(0));
		}
		else{
			time++;
			this.running();
		}
	}
	
	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
