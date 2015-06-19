package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.the.machine.behaviours.MapCoverBehaviour;
import com.the.machine.behaviours.MapCoverBehaviour.MapCoverBehaviourState;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.components.BehaviourComponent.BehaviourResponse;
import com.the.machine.systems.ActionSystem;

import java.util.List;

public class MapCoverLeaf extends LeafTask<TreeContext>{

	private MapCoverBehaviour mcb;
	private MapCoverBehaviourState state;

	public MapCoverLeaf(){
		this.mcb = new MapCoverBehaviour();
		this.state = null;
	}

	@Override
	public void run(TreeContext context) {
		List<BehaviourResponse> responses = mcb.evaluate(context.getBehaviorContext(), state);
		for(BehaviourResponse res : responses){
			if (res.getAction() == ActionSystem.Action.STATE) {
				state = (MapCoverBehaviourState) ((ActionSystem.StateData) res.getData()).getState();
			} else {
				context.addResponse(res);
			}
		}
		this.success();
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		return null;
	}

}
