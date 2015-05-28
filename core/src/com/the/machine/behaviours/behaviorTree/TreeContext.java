package com.the.machine.behaviours.behaviorTree;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.behaviorTree.TreeBehavior.TreeBehaviorState;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.BehaviourComponent.BehaviourContext;
import com.the.machine.components.BehaviourComponent.BehaviourResponse;
import com.the.machine.systems.ActionSystem;

@Data
/** This Class Contains All The Context Which Is Used For Operations Concerning Behavior Trees */
public class TreeContext {
	
	/** The Actual Behavior Context */
	private BehaviourContext behaviorContext;
	
	/** Target Location Can Be Declared To Then later be used */
	private Vector2 targetLocation;
	
	/** List Storing All Responses To Be Taken This Iteration */
	private List<BehaviourResponse> responseList;
	
	
	
	/** Constructor */
	public TreeContext(){
		this.responseList = new ArrayList<BehaviourResponse>();
	}
	
	/** Adds New Response To List */
	public void addResponse(BehaviourResponse response){
		this.responseList.add(response);
	}
	
	/** Adds The Response Consisting Off The Given Action And The Corresponding Data Of The Action (Nothing Else, Blame Fabian!) */
	public void addResponse(ActionSystem.Action action, Object actionData){
		this.responseList.add( new BehaviourResponse<TreeBehaviorState>(action, actionData));
	}
	
	/** Clears The Response list */
	public void clearResponseList(){
		this.responseList.clear();
	}
	
}
