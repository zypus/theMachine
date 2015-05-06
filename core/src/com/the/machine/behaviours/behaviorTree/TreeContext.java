package com.the.machine.behaviours.behaviorTree;

import lombok.Data;

import com.the.machine.behaviours.behaviorTree.TreeBehavior.TreeBehaviorState;
import com.the.machine.components.BehaviourComponent;

@Data
/** This Class Contains All The Context Which Is Used For Operations Concerning Behavior Trees */
public class TreeContext {
	
	private BehaviourComponent.BehaviourResponse<TreeBehaviorState> response;
	
}
