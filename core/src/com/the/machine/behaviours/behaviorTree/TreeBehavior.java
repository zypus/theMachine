package com.the.machine.behaviours.behaviorTree;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.RandomBehaviour;
import com.the.machine.behaviours.RandomBehaviour.RandomBehaviourState;
import com.the.machine.behaviours.behaviorTree.TreeBehavior.TreeBehaviorState;
import com.the.machine.behaviours.behaviorTree.leafTasks.TestLeafTimer;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.PathfindingLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.RandomMovement;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.ResLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.TurnToTargetLocationLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.ifLeaf.HearingLeaf;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.BehaviourComponent.BehaviourContext;
import com.the.machine.components.BehaviourComponent.BehaviourResponse;
import com.the.machine.systems.ActionSystem;

public class TreeBehavior implements BehaviourComponent.Behaviour<TreeBehavior.TreeBehaviorState> {
	
	private BehaviorTree<TreeContext> tree;
	
	public TreeBehavior(){
		int time = 5;
		float speed = 50;
		this.tree = new BehaviorTree<TreeContext>();
		List<Task<TreeContext>> list = new ArrayList<>();
		
		list.add(new RandomMovement());
		list.add(new TurnToTargetLocationLeaf(speed));
		list.add(new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(speed)));
		
		List<Task<TreeContext>> list2 = new ArrayList<>();
		
		list2.add(new HearingLeaf());
		list2.add(new TurnToTargetLocationLeaf(speed));
		list2.add(new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(speed)));
		
		Task<TreeContext> normal = new Sequence<TreeContext>(list.toArray( new Task[list.size()]));
		Task<TreeContext> sound = new Sequence<TreeContext>(list2.toArray( new Task[list2.size()]));
		Task<TreeContext> seq = new Sequence<TreeContext>(new PathfindingLeaf(), new TurnToTargetLocationLeaf(speed), new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(speed)));
		
		
		tree.addChild((seq));
		TreeContext treeContext = new TreeContext();
		tree.setObject(treeContext);
	}

	@Override
	public List<BehaviourResponse> evaluate(BehaviourContext context, TreeBehaviorState state) {
		List<BehaviourResponse> responseList = new ArrayList<BehaviourResponse>();
		TreeContext treeContext = tree.getObject();
		treeContext.setDestination(new Vector2(1, 1));
		if(!treeContext.isInited()){
			treeContext.init(context);
		}
		treeContext.clearResponseList();
		treeContext.setBehaviorContext(context);
		tree.step();
		treeContext.update();
		responseList = tree.getObject().getResponseList();
		return responseList;
	}
	
	@AllArgsConstructor
	public static class TreeBehaviorState implements BehaviourComponent.BehaviourState{
		
	}

}
