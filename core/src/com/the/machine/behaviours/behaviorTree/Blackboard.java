package com.the.machine.behaviours.behaviorTree;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import com.badlogic.ashley.core.Entity;

/** Object used by all agents to communicate with other agents of their type */
public class Blackboard {
	
	/** List containing all the Agents CURRENTLY seen */
	@Getter
	private List<WeakReference<Entity>> seenAgentsList;
	
	public Blackboard(){
		this.seenAgentsList = new ArrayList<WeakReference<Entity>>();
	}
	
	
	public void update(){
		this.seenAgentsList.clear();
	}
	
}
