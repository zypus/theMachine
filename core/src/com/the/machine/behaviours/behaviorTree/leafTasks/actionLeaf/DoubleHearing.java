package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.behaviorTree.TreeContext;

/** Triggers For Two Sound events for same general direction within close proximity */
public class DoubleHearing extends LeafTask<TreeContext>{
	
	private static final int DELETION_THRESHOLD = 100;
	
	private long timeStep;
	
	private List<Vector2> recentSounds;
	private List<Long> recentSoundTimeSteps;
	
	public DoubleHearing(){
		this.timeStep = 0;
		this.recentSounds = new ArrayList<Vector2>();
		this.recentSoundTimeSteps = new ArrayList<Long>();
	}
	
	@Override
	public void run(TreeContext context) {
		addNewSound(context);
		timeStep++;
		deleteSounds();
	}
	
	private void addNewSound(TreeContext context){
		float EPSILON = 0.00001f;
		List<Vector2> potNewSounds = context.getBehaviorContext().getSoundDirections();
		for(Vector2 newSound : potNewSounds){
			boolean alreadyIn = false;
			for(int c2=0; c2<recentSounds.size(); c2++){
				if(Math.abs(recentSounds.get(c2).x-newSound.x)<EPSILON && Math.abs(recentSounds.get(c2).x-newSound.x)<EPSILON){
					alreadyIn = true;
				}
			}
			if(!alreadyIn){
				add(newSound);
			}
		}
	}
	
	
	
	private void deleteSounds(){
		for(int c=0; c<recentSounds.size(); c++){
			if(recentSoundTimeSteps.get(c) < timeStep-DELETION_THRESHOLD){
				remove(c);
			}
		}
	}
	
	private void add(Vector2 sound){
		this.recentSounds.add(sound);
		this.recentSoundTimeSteps.add(timeStep);
	}
	
	private void remove(int index){
		this.recentSounds.remove(index);
		this.recentSoundTimeSteps.remove(index);
	}
	
	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		return null;
	}
	
}
