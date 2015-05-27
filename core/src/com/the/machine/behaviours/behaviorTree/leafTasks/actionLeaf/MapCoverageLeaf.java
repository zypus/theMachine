package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.map.Mapper;

public class MapCoverageLeaf extends LeafTask<TreeContext>{
	
	private Mapper mapper;

	@Override
	public void run(TreeContext context) {
		if(this.mapper==null){
			initMapper(context);
		}
	}
	
	private void initMapper(TreeContext context){
		
	}
	
	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		return null;
	}

}
