package com.the.machine.behaviours.behaviorTree;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AreaComponent.AreaType;
import com.the.machine.components.DiscreteMapComponent.MapCell;

/** Object used by all agents to communicate with other agents of their type */
public class Blackboard {
	
	/** References to the Agent's Context */
	private List<TreeContext> contextReferences;
	
	/** List containing all the Agents CURRENTLY seen */
	@Getter private List<List<WeakReference<Entity>>> seenAgentsLists;
	/** List containing all the Agents relative Hearing */
	@Getter private List<List<Vector2>> hearedAgentsList;
	
	public Blackboard(){
		this.contextReferences = new ArrayList<TreeContext>();
		this.seenAgentsLists = new ArrayList<List<WeakReference<Entity>>>();
		this.hearedAgentsList = new ArrayList<List<Vector2>>();
	}
	
	/** Returns List With All The Weak References Of Agents Stored Within It, Non Redundantly */
	public List<WeakReference<Entity>> getSeenAgentList(){
		List<WeakReference<Entity>> list = new ArrayList<WeakReference<Entity>>();
		for(List<WeakReference<Entity>> seenAgent : seenAgentsLists){
			for(WeakReference<Entity> entity : seenAgent){
				boolean notInList = true;
				for(WeakReference<Entity> entityInList : list){
					if(entityInList.get().equals(entity.get())){
						notInList = false;
						break;
					}
				}
				if(notInList){
					list.add(entity);
				}
			}
		}
		return list;
	}
	
	/** Adds And Replaces The Given List To The Complete List, Depending on the identitiy of the agent that has given its context here */
	public void addAndReplaceAgentList(List<WeakReference<Entity>> list, TreeContext context){
		for(int c=0; c<contextReferences.size(); c++){
			if(context.getId() == contextReferences.get(c).getId()){
				this.seenAgentsLists.set(c, list);
				this.hearedAgentsList.set(c, context.getBehaviorContext().getSoundDirections());
				return;
			}
		}
		this.contextReferences.add(context);
		this.hearedAgentsList.add(context.getBehaviorContext().getSoundDirections());
		this.seenAgentsLists.add(list);
	}
	
	public void update(){
		
	}
	
}
