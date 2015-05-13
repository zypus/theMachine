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
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.ResLeaf;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.BehaviourComponent.BehaviourContext;
import com.the.machine.components.BehaviourComponent.BehaviourResponse;
import com.the.machine.systems.ActionSystem;

public class TreeBehavior implements BehaviourComponent.Behaviour<TreeBehavior.TreeBehaviorState> {
	
	private BehaviorTree<TreeContext> tree;
	
	public TreeBehavior(){
		this.tree = new BehaviorTree<TreeContext>();
		Task<TreeContext>[] list = new Task[5];
		list[0] = new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(1f));
		list[1] = new TestLeafTimer(300);
		list[2] = new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(0));
		list[3] = new ResLeaf(ActionSystem.Action.TURN, new ActionSystem.TurnData(44, 44));
		list[4] = new TestLeafTimer(300);
		Task<TreeContext> seq = new Sequence<TreeContext>(list);
		tree.addChild(new com.badlogic.gdx.ai.btree.decorator.UntilFail<TreeContext>(seq));
		TreeContext treeContext = new TreeContext();
		tree.setObject(treeContext);
	}

	@Override
	public List<BehaviourResponse> evaluate(BehaviourContext context, TreeBehaviorState state) {
		List<BehaviourResponse> responseList = new ArrayList<BehaviourResponse>();
		tree.getObject().clearResponseList();
		tree.step();
		responseList = tree.getObject().getResponseList();
		return responseList;
	}
	
	@AllArgsConstructor
	public static class TreeBehaviorState implements BehaviourComponent.BehaviourState{
		
	}

}
