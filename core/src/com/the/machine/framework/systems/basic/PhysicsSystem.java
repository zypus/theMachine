package com.the.machine.framework.systems.basic;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.ObservableComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.physics.PhysicsComponent;
import com.the.machine.framework.interfaces.Observable;
import com.the.machine.framework.interfaces.Observer;
import com.the.machine.framework.utility.EntityUtilities;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 07/03/15
 */
public class PhysicsSystem
		extends IteratingSystem implements EntityListener, Observer {

	transient private ComponentMapper<PhysicsComponent> physicObjects = ComponentMapper.getFor(PhysicsComponent.class);
	transient private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);

	transient private World box2dWorld;

	transient private static float BOX_TO_WORLD = 10;
	transient private static float WORLD_TO_BOX = 1f / BOX_TO_WORLD;

	transient private boolean updating = false;

	public PhysicsSystem() {
		super(Family.all(PhysicsComponent.class, TransformComponent.class)
					.get());
		box2dWorld = new World(new Vector2(0,0), true);
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(Family.all(PhysicsComponent.class, TransformComponent.class).get(), this);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}

	@Override
	public void entityAdded(Entity entity) {
		makeBody(entity);
		transforms.get(entity).addObserver(this);
	}

	@Override
	public void entityRemoved(Entity entity) {
		PhysicsComponent physicsComponent = physicObjects.get(entity);
		Body body = physicsComponent.getBody();
		box2dWorld.destroyBody(body);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (!updating) {
			ObservableComponent component = (ObservableComponent) o;
			Entity entity = component.getOwner()
									 .get();
			if (entity != null) {
				TransformComponent transformComponent = transforms.get(entity);
				PhysicsComponent physicsComponent = physicObjects.get(entity);
				Body body = physicsComponent.getBody();
				if (body != null) {
					body.setTransform(transformComponent.get2DPosition().scl(WORLD_TO_BOX), transformComponent.getZRotation());
				}
			}
		}
	}

	@Override
	public void update(float deltaTime) {
		// FIXME I don't know yet what are good values for the iterations, for now I set them arbitrarily to 5
		box2dWorld.step(deltaTime, 5, 5);
		updating = true;
		super.update(deltaTime);
		updating = false;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		PhysicsComponent physicsComponent = physicObjects.get(entity);
		Body body = physicsComponent.getBody();
		TransformComponent transformComponent = transforms.get(entity);
		transformComponent.set2DPosition(body.getPosition()
											 .cpy().scl(BOX_TO_WORLD));
		transformComponent.setZRotation(body.getAngle());
		transformComponent.notifyObservers();
	}

	private void makeBody(Entity entity) {
		TransformComponent transformComponent = EntityUtilities.computeAbsoluteTransform(entity);
		PhysicsComponent physicsComponent = physicObjects.get(entity);
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(transformComponent.get2DPosition().scl(WORLD_TO_BOX));
		bodyDef.angle = transformComponent.getZRotation();
		bodyDef.type = BodyDef.BodyType.DynamicBody;

		Body body = box2dWorld.createBody(bodyDef);
		physicsComponent.setBody(body);
	}
}
