package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import java.util.List;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.MapCoverBehaviour;
import com.the.machine.behaviours.MapCoverBehaviour.MapCoverBehaviourState;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.components.BehaviourComponent.BehaviourResponse;

public class MapCoverLeaf extends LeafTask<TreeContext>{
	
	private MapCoverBehaviour mcb;
	private MapCoverBehaviourState state;
	
	public MapCoverLeaf(){
		this.mcb = new MapCoverBehaviour();
		this.state = new MapCoverBehaviourState();
	}

	@Override
	public void run(TreeContext context) {
		List<BehaviourResponse> responses = mcb.evaluate(context.getBehaviorContext(), state);
		for(BehaviourResponse res : responses){
			context.addResponse(res);
		}
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		return null;
	}

}
