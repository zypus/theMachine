package com.the.machine.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.framework.components.AbstractComponent;
import com.the.machine.systems.ActionSystem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Frans on 12-3-2015.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BehaviourComponent<T extends BehaviourComponent.BehaviourState> extends AbstractComponent {

	Behaviour<T> behaviour;
	T state;

	public static interface Behaviour<T extends BehaviourState> {
		BehaviourResponse<T> evaluate(BehaviourContext context, T state);
	}

	public static interface BehaviourState{};

	@Data
	@AllArgsConstructor
	public static class BehaviourContext {
		float pastTime;
		float currentMovementSpeed;
		float currentTurningSpeed;
		Vector2 moveDirection;
		AreaComponent.AreaType environment;
		List<DiscreteMapComponent.MapCell> vision;
		List<WeakReference<Entity>> agents;
		List<Vector2>               soundDirections;
		boolean                     canSprint;
		float                       sprintTime;
		float                       sprintCooldown;
		boolean                     hidden;
		boolean                     inTower;
	}

	@Data
	@AllArgsConstructor
	public static class BehaviourResponse<T extends BehaviourState> {
		
		private static final float MAX_CLEAR_VISION_TURNING = 45;
		private static final float MAX_TURNING = 180;
		
		private float                     movementSpeed;
		private float                     turningSpeed;
		private List<ActionSystem.Action> actions;
		private T                         nextBehaviourState;
		private int markerNumber;
		
		public void setTurning(BehaviourContext context, Vector2 dir, float desSpeed, boolean allowBlurryVision){
			float angle = context.getMoveDirection().angle(dir);
			if(angle!=0){
				angle = angle/Math.abs(angle);
			}
			angle = angle * desSpeed;
			if(!allowBlurryVision){
				this.turningSpeed = MathUtils.clamp(angle, -MAX_CLEAR_VISION_TURNING, MAX_CLEAR_VISION_TURNING);
			}
			else{
				this.turningSpeed = MathUtils.clamp(angle, -MAX_TURNING, MAX_TURNING);
			}
		}
		
	}

}
