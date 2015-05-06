package com.the.machine.behaviours.behaviorTree;

import java.util.ArrayList;

import lombok.AllArgsConstructor;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.the.machine.behaviours.RandomBehaviour;
import com.the.machine.behaviours.RandomBehaviour.RandomBehaviourState;
import com.the.machine.behaviours.behaviorTree.leafTasks.TestLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.TestLeafTimer;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.BehaviourComponent.BehaviourContext;
import com.the.machine.components.BehaviourComponent.BehaviourResponse;

public class TreeBehavior implements BehaviourComponent.Behaviour<TreeBehavior.TreeBehaviorState> {
	
	private BehaviorTree<TreeContext> tree;
	
	public TreeBehavior(){
		this.tree = new BehaviorTree<TreeContext>();
		tree.addChild(new Sequence<TreeContext>(new TestLeafTimer(100), new TestLeaf()));
	}

	@Override
	public BehaviourResponse<TreeBehaviorState> evaluate(BehaviourContext context, TreeBehaviorState state) {
		BehaviourComponent.BehaviourResponse<TreeBehaviorState> response = new BehaviourComponent.BehaviourResponse<>(context.getCurrentMovementSpeed(), context.getCurrentTurningSpeed(), new ArrayList<>(), state, 0);
		TreeContext treeContext = new TreeContext();
		treeContext.setResponse(response);
		tree.setObject(treeContext);
		tree.step();
		return response;
	}
	
	@AllArgsConstructor
	public static class TreeBehaviorState implements BehaviourComponent.BehaviourState{
		
	}

}
