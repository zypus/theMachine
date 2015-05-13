package com.the.machine.behaviours.behaviorTree;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.math.Vector2;
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
		int time = 5;
		this.tree = new BehaviorTree<TreeContext>();
		List<Task<TreeContext>> list = new ArrayList<>();
		list.add(new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(10f)));
		list.add(new TestLeafTimer(time));
		list.add(new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(0)));
		list.add(new ResLeaf(ActionSystem.Action.TURN, new ActionSystem.TurnData(new Vector2(1, 1), 30)));
		list.add(new TestLeafTimer(time));
		list.add(new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(10f)));
		list.add(new TestLeafTimer(time));
		list.add(new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(0)));
		list.add(new ResLeaf(ActionSystem.Action.TURN, new ActionSystem.TurnData(new Vector2(1, -1), 30)));
		list.add(new TestLeafTimer(time));
		list.add(new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(10f)));
		list.add(new TestLeafTimer(time));
		list.add(new ResLeaf(ActionSystem.Action.TOWER_ENTER, "Poopi"));
		list.add(new TestLeafTimer(time));
		list.add(new ResLeaf(ActionSystem.Action.MARKER_PLACE, new ActionSystem.MarkerData(4, 0.8f)));
		list.add(new TestLeafTimer(time));
		list.add(new ResLeaf(ActionSystem.Action.TOWER_LEAVE, "Poopi"));
		Task<TreeContext> seq = new Sequence<TreeContext>(list.toArray( new Task[list.size()]));
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
		for(BehaviourResponse b : responseList){
			System.out.print(b.getAction());
		}
		System.out.println();
		return responseList;
	}
	
	@AllArgsConstructor
	public static class TreeBehaviorState implements BehaviourComponent.BehaviourState{
		
	}

}
