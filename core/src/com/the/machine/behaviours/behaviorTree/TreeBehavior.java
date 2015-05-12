package com.the.machine.behaviours.behaviorTree;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.the.machine.behaviours.RandomBehaviour;
import com.the.machine.behaviours.RandomBehaviour.RandomBehaviourState;
import com.the.machine.behaviours.behaviorTree.TreeBehavior.TreeBehaviorState;
import com.the.machine.behaviours.behaviorTree.leafTasks.TestLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.TestLeafTimer;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.BehaviourComponent.BehaviourContext;
import com.the.machine.components.BehaviourComponent.BehaviourResponse;
import com.the.machine.systems.ActionSystem;

public class TreeBehavior implements BehaviourComponent.Behaviour<TreeBehavior.TreeBehaviorState> {
	
	private BehaviorTree<TreeContext> tree;
	
	public TreeBehavior(){
		this.tree = new BehaviorTree<TreeContext>();
		tree.addChild(new Sequence<TreeContext>(new TestLeafTimer(100), new TestLeaf()));
	}

	@Override
	public List<BehaviourResponse> evaluate(BehaviourContext context, TreeBehaviorState state) {
		List<BehaviourResponse> responseList = new ArrayList<BehaviourResponse>();
		TreeContext treeContext = new TreeContext();
		tree.start(treeContext);
		tree.step();
		responseList = treeContext.getResponseList();
		return responseList;
	}
	
	@AllArgsConstructor
	public static class TreeBehaviorState implements BehaviourComponent.BehaviourState{
		
	}

}
