package com.the.machine.behaviours.behaviorTree;

import java.util.ArrayList;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.the.machine.behaviours.RandomBehaviour;
import com.the.machine.behaviours.RandomBehaviour.RandomBehaviourState;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.BehaviourComponent.BehaviourContext;
import com.the.machine.components.BehaviourComponent.BehaviourResponse;

public class TreeBehavior implements BehaviourComponent.Behaviour<RandomBehaviour.RandomBehaviourState> {

	@Override
	public BehaviourResponse<RandomBehaviourState> evaluate(BehaviourContext context, RandomBehaviourState state) {
		BehaviourComponent.BehaviourResponse<RandomBehaviourState> response = new BehaviourComponent.BehaviourResponse<>(context.getCurrentMovementSpeed(), context.getCurrentTurningSpeed(), new ArrayList<>(), state, 0);
		TreeContext treeContext = new TreeContext();
		treeContext.setResponse(response);
		BehaviorTree<TreeContext> tree = new BehaviorTree<TreeContext>();
		
		
		return response;
	}

}
