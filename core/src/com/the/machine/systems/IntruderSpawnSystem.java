package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.the.machine.behaviours.RandomBehaviour;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.AngularVelocityComponent;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.DizzinessComponent;
import com.the.machine.components.DraggableComponent;
import com.the.machine.components.EntryPointComponent;
import com.the.machine.components.ListenerComponent;
import com.the.machine.components.MapGroundComponent;
import com.the.machine.components.SelectableComponent;
import com.the.machine.components.SprintComponent;
import com.the.machine.components.StepComponent;
import com.the.machine.components.VelocityComponent;
import com.the.machine.components.VisionComponent;
import com.the.machine.events.AudioEvent;
import com.the.machine.framework.IntervalSystem;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.NameComponent;
import com.the.machine.framework.components.ShapeRenderComponent;
import com.the.machine.framework.components.SpriteRenderComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.physics.ColliderComponent;
import com.the.machine.framework.components.physics.Light2dComponent;
import com.the.machine.framework.components.physics.Physics2dComponent;
import com.the.machine.framework.utility.BitBuilder;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/03/15
 */
public class IntruderSpawnSystem
		extends IntervalSystem {

	private static final int INTRUDER_COUNT = 1;

	transient private ComponentMapper<DimensionComponent> dimensions = ComponentMapper.getFor(DimensionComponent.class);

	transient private ImmutableArray<Entity> intruders;
	transient private ImmutableArray<Entity> ground;

	public IntruderSpawnSystem() {
		super(1);
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		intruders = world.getEntitiesFor(Family.all(SprintComponent.class)
											   .get());
		ground = world.getEntitiesFor(Family.all(MapGroundComponent.class)
											.get());
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		intruders = null;
	}

	@Override
	protected void updateInterval() {
		int size = intruders.size();
		if (size < INTRUDER_COUNT && ground.size() > 0) {
			for (int i = 0; i < INTRUDER_COUNT - size; i++) {
				spawnIntruder();
			}
		}
	}

	private void spawnIntruder() {
		Entity first = ground.first();
		DimensionComponent dm = dimensions.get(first);

		float spawnX;
		float spawnY;
		if (MathUtils.random() < 0.5) {
			if (MathUtils.random() < 0.5) {
				spawnX = -dm.getWidth() / 2 + 1;
			} else {
				spawnX = dm.getWidth() / 2 - 1;
			}
			spawnY = MathUtils.random() * dm.getHeight() - dm.getHeight() / 2;
		} else {
			if (MathUtils.random() < 0.5) {
				spawnY = -dm.getHeight() / 2 + 1;
			} else {
				spawnY = dm.getHeight() / 2 - 1;
			}
			spawnX = MathUtils.random() * dm.getWidth() - dm.getWidth() / 2;
		}

		float angle = new Vector2(spawnX, spawnY).scl(-1)
												 .angle();

		Entity newAgent = new Entity();
		newAgent.add(new TransformComponent().setPosition(spawnX, spawnY, 0).setZRotation(angle));
		newAgent.add(new DimensionComponent().setDimension(1, 1));
		newAgent.add(new LayerComponent(BitBuilder.none(32)
												  .s(1)
												  .get()));
		newAgent.add(new SpriteRenderComponent().setTextureRegion(Asset.fetch("agent", TextureRegion.class))
												.setSortingLayer("Default").setTint(Color.RED));
		newAgent.add(new Physics2dComponent().setType(BodyDef.BodyType.DynamicBody));

		Filter filter = new Filter();
		filter.categoryBits = AreaComponent.AGENT_CATEGORY;
		filter.maskBits = AreaComponent.AGENT_MASK;
		filter.groupIndex = 0;

		Filter lightFilter = new Filter();
		lightFilter.categoryBits = AreaComponent.AGENT_CATEGORY;
		lightFilter.maskBits = AreaComponent.LIGHT_MASK;
		lightFilter.groupIndex = 0;

		newAgent.add(new ColliderComponent().add(new ColliderComponent.Collider().setShape(new Vector2(), 0.5f)
																				 .setFilter(filter)));
		newAgent.add(new SelectableComponent());
		newAgent.add(new DraggableComponent());
		newAgent.add(new Light2dComponent().setType(Light2dComponent.LightType.CONE)
										   .setAngle(45)
										   .setColor(Color.RED)
										   .setFilter(lightFilter)
										   .setDistance(7.5f));
		newAgent.add(new VisionComponent());
		newAgent.add(new ShapeRenderComponent().add(new VisionRangeDebugSystem.VisionRangeDebug(0, 7.5f, 45, 10, 18)));
		newAgent.add(new ListenerComponent());
		newAgent.add(new NameComponent().setName("Intruder"));
		newAgent.add(new BehaviourComponent<RandomBehaviour.RandomBehaviourState>().setBehaviour(new RandomBehaviour())
																				   .setState(new RandomBehaviour.RandomBehaviourState(0, 0)));
		newAgent.add(new VelocityComponent());
		newAgent.add(new AngularVelocityComponent());
		newAgent.add(new AgentComponent().setBaseViewingDistance(7.5f));
		newAgent.add(new StepComponent());
		newAgent.add(new DizzinessComponent());
		newAgent.add(new SprintComponent());
		world.addEntity(newAgent);
		world.dispatchEvent(new AudioEvent(new Vector3(spawnX, spawnY, 0), 5, new WeakReference<>(newAgent)));
		Entity spawnPoint = new Entity();
		spawnPoint.add(new TransformComponent().setPosition(spawnX, spawnY, 0));
		spawnPoint.add(new EntryPointComponent().setSpawned(new WeakReference<>(newAgent)));
		world.addEntity(spawnPoint);
	}
}
