package com.the.machine.behaviours.behaviorTree.leafTasks.subTrees;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.MoveUntilLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.NegateTargetLocationLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.PathfindingLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.ResLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.TargetAgentLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.TargetAreaLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.TurnToTargetLocationLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.ifLeaf.HearingLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.ifLeaf.WaitTurnMove;
import com.the.machine.systems.ActionSystem;

@Getter
@AllArgsConstructor
public enum SubTrees {
	//Pathfinds towards destination
	PATHFINDING(new Sequence<TreeContext>(new PathfindingLeaf(), new WaitTurnMove(20), new MoveUntilLeaf(50))),
	//Chases an intruder provided that it either hears or preferably sees him
	DIRECT_VISIBLE_CHASE(new Sequence<TreeContext>(new Selector<TreeContext>(new TargetAgentLeaf(), new HearingLeaf()), new TurnToTargetLocationLeaf(200), new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(50)))),
	DIRECT_VISIBLE_FLEE(new Sequence<TreeContext>(new Selector<TreeContext>(new TargetAgentLeaf(), new HearingLeaf()), new NegateTargetLocationLeaf(), new TurnToTargetLocationLeaf(200), new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(50)))),
	GO_TO_TARGET(new Sequence<TreeContext>(new TargetAreaLeaf(), new TurnToTargetLocationLeaf(200), new ResLeaf(ActionSystem.Action.MOVE, new ActionSystem.MoveData(50))));
	
	private Task<TreeContext> subtree;

}
