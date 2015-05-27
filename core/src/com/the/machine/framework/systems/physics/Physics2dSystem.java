package com.the.machine.framework.systems.physics;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.ObservableComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.physics.ColliderComponent;
import com.the.machine.framework.components.physics.Physics2dComponent;
import com.the.machine.framework.components.physics.Physics2dDebugComponent;
import com.the.machine.framework.events.physics.ContactBeginEvent;
import com.the.machine.framework.events.physics.ContactEndEvent;
import com.the.machine.framework.interfaces.Observable;
import com.the.machine.framework.interfaces.Observer;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.EntityUtilities;
import lombok.Data;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 07/03/15
 */
@Data
public class Physics2dSystem
		extends IteratingSystem
		implements EntityListener, Observer, ContactListener {

	transient private ComponentMapper<Physics2dComponent> physicObjects = ComponentMapper.getFor(Physics2dComponent.class);
	transient private ComponentMapper<TransformComponent> transforms    = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<ColliderComponent>  colliders     = ComponentMapper.getFor(ColliderComponent.class);

	transient private static float BOX_TO_WORLD = 10;
	transient private static float WORLD_TO_BOX = 1f / BOX_TO_WORLD;

	transient private boolean updating = false;

	public Physics2dSystem() {
		super(Family.all(Physics2dComponent.class, TransformComponent.class)
					.get());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(Family.all(Physics2dComponent.class, TransformComponent.class)
									   .get(), this);
		if (world.getBox2dWorld() == null) {
			world.setBox2dWorld(new World(new Vector2(0, 0), true));
		}
		world.getBox2dWorld().setContactListener(this);
		Entity debugEntity = new Entity();
		debugEntity.add(new Physics2dDebugComponent().setBox2dWorld(world.getBox2dWorld())
													 .setBoxToWorld(BOX_TO_WORLD));
		debugEntity.add(new LayerComponent(BitBuilder.none(32)
													 .s(2)
													 .get()));
		world.addEntity(debugEntity);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
		ImmutableArray<Entity> entities = world.getEntitiesFor(Family.all(Physics2dDebugComponent.class)
																	 .get());
		for (Entity entity : entities) {
			world.removeEntity(entity);
		}
		world.getBox2dWorld().setContactListener(null);
	}

	@Override
	public void entityAdded(Entity entity) {
		makeBody(entity);
		transforms.get(entity)
				  .addObserver(this);
	}

	@Override
	public void entityRemoved(Entity entity) {
		Physics2dComponent physics2dComponent = physicObjects.get(entity);
		Body body = physics2dComponent.getBody();
		world.getBox2dWorld()
			 .destroyBody(body);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (!updating) {
			ObservableComponent component = (ObservableComponent) o;
			Entity entity = component.getOwner()
									 .get();
			if (entity != null) {
				TransformComponent transformComponent = transforms.get(entity);
				Physics2dComponent physics2dComponent = physicObjects.get(entity);
				Body body = physics2dComponent.getBody();
				if (body != null) {
					body.setTransform(transformComponent.get2DPosition()
														.scl(WORLD_TO_BOX), MathUtils.degreesToRadians * transformComponent.getZRotation());
				}
			}
		}
	}

	private void updateBodies() {
		for (Entity entity : getEntities()) {
			Physics2dComponent physics2dComponent = physicObjects.get(entity);
			Body body = physics2dComponent.getBody();
			body.setType(physics2dComponent.getType());
			if (colliders.has(entity)) {
				ColliderComponent colliderComponent = colliders.get(entity);
				for (ColliderComponent.Collider collider : colliderComponent.getColliders()) {
					if (collider.getFixture() == null) {
						makeFixture(body, collider);
					}
					else if (collider.isChanged()) {
						if (collider.isShapeChanged()) {
							body.destroyFixture(collider.getFixture());
							makeFixture(body, collider);
						} else {
							Fixture fixture = collider.getFixture();
							fixture.setDensity(collider.getDensity());
							fixture.setFilterData(collider.getFilter());
							fixture.setFriction(collider.getFriction());
							fixture.setRestitution(collider.getRestitution());
							fixture.setSensor(collider.isSensor());
						}
					}
				}
				if (!colliderComponent.getRemoved()
									  .isEmpty()) {
					for (ColliderComponent.Collider collider : colliderComponent.getRemoved()) {
						body.destroyFixture(collider.getFixture());
					}
					colliderComponent.getRemoved()
									 .clear();
				}
			}
		}
	}

	@Override
	public void update(float deltaTime) {
		updateBodies();
		// FIXME I don't know yet what are good values for the iterations, for now I set them arbitrarily to 5
		world.getBox2dWorld()
			 .step(deltaTime, 5, 5);
		updating = true;
		super.update(deltaTime);
		updating = false;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		Physics2dComponent physics2dComponent = physicObjects.get(entity);
		Body body = physics2dComponent.getBody();
		TransformComponent transformComponent = transforms.get(entity);
		transformComponent.set2DPosition(body.getPosition()
											 .cpy()
											 .scl(BOX_TO_WORLD));
		transformComponent.setZRotation(body.getAngle() * MathUtils.radiansToDegrees);
		transformComponent.notifyObservers();
	}

	private void makeBody(Entity entity) {
		TransformComponent transformComponent = EntityUtilities.computeAbsoluteTransform(entity);
		Physics2dComponent physics2dComponent = physicObjects.get(entity);
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(transformComponent.get2DPosition()
											   .scl(WORLD_TO_BOX));
		bodyDef.angle = transformComponent.getZRotation();
		bodyDef.type = physics2dComponent.getType();
		bodyDef.fixedRotation = true;
		bodyDef.allowSleep = false;

		Body body = world.getBox2dWorld()
						 .createBody(bodyDef);
		body.setUserData(new WeakReference<>(entity));
		if (colliders.has(entity)) {
			ColliderComponent colliderComponent = colliders.get(entity);
			for (ColliderComponent.Collider collider : colliderComponent.getColliders()) {
				makeFixture(body, collider);
			}
		}
		physics2dComponent.setBody(body);
	}

	private void makeFixture(Body body, ColliderComponent.Collider collider) {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = collider.getDensity();
		fixtureDef.filter.categoryBits = collider.getFilter().categoryBits;
		fixtureDef.filter.groupIndex = collider.getFilter().groupIndex;
		fixtureDef.filter.maskBits = collider.getFilter().maskBits;
		fixtureDef.friction = collider.getFriction();
		fixtureDef.isSensor = collider.isSensor();
		fixtureDef.shape = collider.getShape()
								   .createShape();
		Fixture fixture = body.createFixture(fixtureDef);
		collider.setFixture(fixture);
	}

	@Override
	public void beginContact(Contact contact) {

		world.dispatchEvent(new ContactBeginEvent((WeakReference<Entity>) contact.getFixtureA()
																				 .getBody()
																				 .getUserData(), (WeakReference<Entity>) contact.getFixtureB()
																																.getBody()
																																.getUserData()));
	}

	@Override
	public void endContact(Contact contact) {

		world.dispatchEvent(new ContactEndEvent((WeakReference<Entity>) contact.getFixtureA()
																			   .getBody()
																			   .getUserData(), (WeakReference<Entity>) contact.getFixtureB()
																															  .getBody()
																															  .getUserData()));
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {

	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {

	}
}
