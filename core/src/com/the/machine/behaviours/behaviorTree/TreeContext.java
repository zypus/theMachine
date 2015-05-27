package com.the.machine.behaviours.behaviorTree;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.behaviorTree.TreeBehavior.TreeBehaviorState;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.BehaviourComponent.BehaviourContext;
import com.the.machine.components.BehaviourComponent.BehaviourResponse;
import com.the.machine.map.Mapper;
import com.the.machine.systems.ActionSystem;

@Data
/** This Class Contains All The Context Which Is Used For Operations Concerning Behavior Trees */
public class TreeContext {
	
	/** Whether this has been inited */
	private boolean inited;
	
	/** The Actual Behavior Context */
	private BehaviourContext behaviorContext;
	
	/** Target Location Can Be Declared To Then later be used */
	private Vector2 targetLocation;
	
	/** List Storing All Responses To Be Taken This Iteration */
	private List<BehaviourResponse> responseList;
	
	/** Mapper Used For Map Coverage */
	private Mapper mapper;
	
	
	
	/** Constructor */
	public TreeContext(){
		this.responseList = new ArrayList<BehaviourResponse>();
		this.inited = false;
		this.mapper = new Mapper();
	}
	
	
	/**Inits Anything that has to be inited */
	public void init(BehaviourContext context){
		this.inited = true;
		mapper.init(new Vector2(0, 0), context.getMoveDirection());
	}
	
	/** Returns whether this Context has been inited */
	public boolean isInited(){
		return this.inited;
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
	
	/** Updates everything in the tree context that needs updateing */
	public void update(){
		if(mapper!=null){
			mapper.update(this.behaviorContext);
		}
	}
}
