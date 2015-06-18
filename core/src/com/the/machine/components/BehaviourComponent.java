package com.the.machine.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.framework.components.AbstractComponent;
import com.the.machine.misc.Placebo;
import com.the.machine.systems.ActionSystem;
import com.the.machine.systems.VisionSystem;
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
public class BehaviourComponent<T extends BehaviourComponent.BehaviourState>
		extends AbstractComponent {

	Behaviour<T> behaviour;
	T state;

	public static interface Behaviour<T extends BehaviourState> {
		List<BehaviourResponse> evaluate(BehaviourContext context, T state);
	}

	public static interface BehaviourState {
	}

	;

	@Data
	@AllArgsConstructor
	public static class BehaviourContext {
		float                                pastTime;
		float                                currentMovementSpeed;
		float                                currentTurningSpeed;
		Vector2                              moveDirection;
		AreaComponent.AreaType               environment;
		List<VisionSystem.EnvironmentVisual> vision;
		List<WeakReference<Entity>>          agents;
		List<WeakReference<Entity>>          markers;
		List<Vector2>                        soundDirections;
		boolean                              canSprint;
		float                                sprintTime;
		float                                sprintCooldown;
		boolean                              hidden;
		boolean                              inTower;
		float                                visionRange;
		float                                visionAngle;
		Placebo                              placebo;
	}

	@Data
	@AllArgsConstructor
	public static class BehaviourResponse<T extends BehaviourState> {
		ActionSystem.Action action;
		Object              data;
	}

}
