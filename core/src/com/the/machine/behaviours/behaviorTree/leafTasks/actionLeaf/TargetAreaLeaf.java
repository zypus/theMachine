package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import java.util.List;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.components.AreaComponent.AreaType;
import com.the.machine.systems.VisionSystem;
import com.the.machine.systems.VisionSystem.EnvironmentVisual;

public class TargetAreaLeaf extends LeafTask<TreeContext>{

	@Override
	public void run(TreeContext context) {
		List<EnvironmentVisual> vis = context.getBehaviorContext().getVision();
		for(EnvironmentVisual v : vis){
			if(v.getType().equals(AreaType.TARGET)){
				context.setTargetRelativeDirection(v.getDelta());
				this.success();
			}
		}
		this.fail();
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}

}
