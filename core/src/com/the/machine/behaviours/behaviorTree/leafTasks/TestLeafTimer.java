package com.the.machine.behaviours.behaviorTree.leafTasks;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.behaviorTree.TreeContext;

public class TestLeafTimer extends LeafTask<TreeContext>{
	
	private int timer;
	
	public TestLeafTimer(int time){
		this.timer = time;
	}

	@Override
	public void run(TreeContext object) {
		if(timer==0){
			success();
		}
		else{
			timer--;
			System.out.println(timer);
			fail();
		}
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}

}
