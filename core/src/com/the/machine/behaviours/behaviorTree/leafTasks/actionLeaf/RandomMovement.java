package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import java.util.Random;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.systems.ActionSystem;

public class RandomMovement extends LeafTask<TreeContext>{
	
	private static final float MAX_RANDOM_MOVE_DURATION = 100;
	
	
	private Vector2 randomMove;
	private float timer;
	private float time;
	
	
	public RandomMovement(){
		setRandomMove();
	}
	

	@Override
	public void run(TreeContext context) {
		if(time == 0){
			context.setTargetRelativeDirection(randomMove);
			super.success();
		}
		else if(time >= timer){
			setRandomMove();
			super.fail();
		}
		else{
			super.fail();
		}
		time += 1;
	}
	
	private void setRandomMove(){
		Random rand = new Random();
		this.timer = rand.nextFloat() * MAX_RANDOM_MOVE_DURATION;
		this.time = 0;
		this.randomMove = new Vector2((rand.nextFloat()*2)-1, (rand.nextFloat()*2)-1);
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
