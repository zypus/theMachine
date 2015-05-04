package com.the.machine.components;

import com.badlogic.ashley.core.Entity;
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
		float                     movementSpeed;
		float                     turningSpeed;
		List<ActionSystem.Action> actions;
		T                         nextBehaviourState;
		int markerNumber;
	}

}
