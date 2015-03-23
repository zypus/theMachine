package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.the.machine.components.StepComponent;
import com.the.machine.components.VelocityComponent;
import com.the.machine.events.AudioEvent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.TransformComponent;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/03/15
 */
public class StepSoundSystem extends IteratingSystem {

	private static final float STEP_SIZE = 1f;

	transient private ComponentMapper<VelocityComponent> velocities = ComponentMapper.getFor(VelocityComponent.class);
	transient private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<StepComponent> steps = ComponentMapper.getFor(StepComponent.class);

	public StepSoundSystem() {
		super(Family.all(VelocityComponent.class, TransformComponent.class, StepComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		VelocityComponent velocityComponent = velocities.get(entity);
		float velocity = Math.abs(velocityComponent.getVelocity());
		StepComponent stepComponent = steps.get(entity);
		stepComponent.setDistanceToNextStep(stepComponent.getDistanceToNextStep() - velocity*deltaTime);
		if (stepComponent.getDistanceToNextStep() <= 0) {
			if (velocity > 0) {
				noiseFunction(entity, velocity);
				stepComponent.setDistanceToNextStep(STEP_SIZE);
			}
		}
	}

	private void noiseFunction(Entity source, float velocity) {
		TransformComponent tf = transforms.get(source);
		if (velocity < 0.5f) {
			world.dispatchEvent(new AudioEvent(tf.getPosition(), 1, new WeakReference<>(source)));
		} else if (velocity < 1) {
			world.dispatchEvent(new AudioEvent(tf.getPosition(), 3, new WeakReference<>(source)));
		} else if (velocity < 2) {
			world.dispatchEvent(new AudioEvent(tf.getPosition(), 5, new WeakReference<>(source)));
		} else {
			world.dispatchEvent(new AudioEvent(tf.getPosition(), 10, new WeakReference<>(source)));
		}
	}
}
