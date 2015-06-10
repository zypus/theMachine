package com.the.machine.behaviours.behaviorTree.leafTasks.subTrees;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.MoveUntilLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf.PathfindingLeaf;
import com.the.machine.behaviours.behaviorTree.leafTasks.ifLeaf.WaitTurnMove;

@Getter
@AllArgsConstructor
public enum SubTrees {
	PATHFINDING(new Sequence<TreeContext>(new PathfindingLeaf(), new WaitTurnMove(20), new MoveUntilLeaf(50)));
	
	private Task<TreeContext> subtree;

}
