package com.the.machine.behaviours.behaviorTree;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.MapCoverLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.MoveUntilLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.OpenDoorLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.PathfindingLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.UpdateBlackboardLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.ifLeaf.WaitTurnMove;
import com.the.machine.behaviours.behaviorTree.leafTasks.subTrees.SubTrees;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.BehaviourComponent.BehaviourContext;
import com.the.machine.components.BehaviourComponent.BehaviourResponse;
import com.the.machine.systems.ActionSystem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class TreeBehavior implements BehaviourComponent.Behaviour<TreeBehavior.TreeBehaviorState> {

	private BehaviorTree<TreeContext> tree;

	//TODO: Do this properly!!!
	private static Blackboard blackboard = new Blackboard();
	@Getter boolean guard = false;

	public TreeBehavior(boolean isGuard){
		if(isGuard){
			guard = true;
			int time = 5;
			float speed = 50;
			this.tree = new BehaviorTree<TreeContext>();
			List<Task<TreeContext>> list = new ArrayList<>();

			list.add(new PathfindingLeaf());
			list.add(new WaitTurnMove(20));
			list.add(new MoveUntilLeaf(50));

			List<Task<TreeContext>> list2 = new ArrayList<>();

			list2.add(new Invert(new WaitTurnMove(20)));

			List<Task<TreeContext>> list3 = new ArrayList<>();


			Task<TreeContext> seq = new Sequence<TreeContext>(new OpenDoorLeaf(), new UpdateBlackboardLeaf(), new Selector<TreeContext>(SubTrees.DIRECT_VISIBLE_CHASE.getSubtree(), new MapCoverLeaf(false)));
//			Task<TreeContext> seq = new Sequence<TreeContext>(new OpenDoorLeaf(), new UpdateBlackboardLeaf(), SubTrees.DIRECT_VISIBLE_CHASE.getSubtree());

			tree.addChild(seq);
			TreeContext treeContext = new TreeContext();
			treeContext.setBlackboard(blackboard);
			tree.setObject(treeContext);
		}
		else{
			int time = 5;
			float speed = 50;
			this.tree = new BehaviorTree<TreeContext>();
			List<Task<TreeContext>> list = new ArrayList<>();

			list.add(new PathfindingLeaf());
			list.add(new WaitTurnMove(20));
			list.add(new MoveUntilLeaf(50));

			List<Task<TreeContext>> list2 = new ArrayList<>();

			list2.add(new Invert(new WaitTurnMove(20)));

			List<Task<TreeContext>> list3 = new ArrayList<>();

			Task<TreeContext> seq = new Sequence<TreeContext>(new OpenDoorLeaf(), new Selector<TreeContext>(SubTrees.DIRECT_VISIBLE_FLEE.getSubtree(), SubTrees.GO_TO_TARGET.getSubtree(), new MapCoverLeaf(true)));

			tree.addChild(seq);
			TreeContext treeContext = new TreeContext();
			treeContext.setBlackboard(blackboard);
			tree.setObject(treeContext);
		}

	}

	@Override
	public List<BehaviourResponse> evaluate(BehaviourContext context, TreeBehaviorState state) {
		List<BehaviourResponse> responseList = new ArrayList<BehaviourResponse>();
		TreeContext treeContext = tree.getObject();
		treeContext.setDestination(new Vector2(10, 10));
		if(!treeContext.isInited()){
			treeContext.init(context);
		}
		treeContext.getBlackboard().update();
		treeContext.clearResponseList();
		treeContext.setBehaviorContext(context);
		tree.step();
		//treeContext.update();
		responseList = tree.getObject().getResponseList();
		responseList.add(new BehaviourResponse(ActionSystem.Action.TOWER_ENTER, null));
		return responseList;
	}

	@AllArgsConstructor
	public static class TreeBehaviorState implements BehaviourComponent.BehaviourState{

	}

}
