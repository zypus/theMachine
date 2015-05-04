package com.the.machine.scenes;

import com.the.machine.events.AudioEvent;
import com.the.machine.framework.SceneBuilder;
import com.the.machine.framework.engine.World;
import com.the.machine.framework.events.basic.AssetLoadingFinishedEvent;
import com.the.machine.framework.events.input.KeyDownEvent;
import com.the.machine.framework.events.input.KeyUpEvent;
import com.the.machine.framework.events.input.ScrolledEvent;
import com.the.machine.framework.events.physics.Light2dToggleEvent;
import com.the.machine.framework.systems.RemovalSystem;
import com.the.machine.framework.systems.physics.Light2dSystem;
import com.the.machine.framework.systems.physics.Physics2dSystem;
import com.the.machine.framework.systems.rendering.CameraRenderSystem;
import com.the.machine.systems.AgentSightSystem;
import com.the.machine.systems.AudioIndicatorSystem;
import com.the.machine.systems.AudioListeningSystem;
import com.the.machine.systems.BehaviourSystem;
import com.the.machine.systems.CameraZoomSystem;
import com.the.machine.systems.DirectionalMovementSystem;
import com.the.machine.systems.GrowthSystem;
import com.the.machine.systems.InputControlledMovementSystem;
import com.the.machine.systems.MovementSystem;
import com.the.machine.systems.RandomBehaviourSystem;
import com.the.machine.systems.RandomNoiseSystem;
import com.the.machine.systems.RotationSystem;
import com.the.machine.systems.SoundDirectionDebugSystem;
import com.the.machine.systems.WorldMappingSystem;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 18/03/15
 */
public class SimulationSceneBuilder
		implements SceneBuilder {
	@Override
	public void createScene(World world) {
		// The systems which act in this scene
		world.addSystem(new RemovalSystem());
		world.addSystem(new GrowthSystem());
		world.addSystem(new DirectionalMovementSystem());
		world.addSystem(new InputControlledMovementSystem(), KeyDownEvent.class, KeyUpEvent.class);
		world.addSystem(new CameraRenderSystem(), AssetLoadingFinishedEvent.class);
		world.addSystem(new CameraZoomSystem(), ScrolledEvent.class);
		world.addSystem(new MovementSystem());
		world.addSystem(new RotationSystem());
		world.addSystem(new RandomNoiseSystem());
		world.addSystem(new AudioIndicatorSystem(), AudioEvent.class);
		world.addSystem(new AudioListeningSystem(), AudioEvent.class);
		world.addSystem(new BehaviourSystem());
		world.addSystem(new RandomBehaviourSystem());
		world.addSystem(new WorldMappingSystem(2)); // Must have a higher priority than AgentSightSystem
		world.addSystem(new AgentSightSystem(1));
		world.addSystem(new SoundDirectionDebugSystem());
		world.addSystem(new Physics2dSystem());
		world.addSystem(new Light2dSystem(), Light2dToggleEvent.class);

		world.loadPrefab("active.map");
	}
}
