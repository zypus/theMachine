package com.the.machine.behaviours.behaviorTree.leafTasks;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.behaviorTree.TreeContext;

public class TestLeafTimer extends LeafTask<TreeContext>{
	
	private int current;
	private int timer;
	
	public TestLeafTimer(int time){
		this.timer = time;
		this.current = timer;
	}

	@Override
	public void run(TreeContext object) {
		if(current==0){
			current = timer;
			success();
			System.out.println("clock out");
		}
		else{
			current--;
			fail();
		}
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}

}
