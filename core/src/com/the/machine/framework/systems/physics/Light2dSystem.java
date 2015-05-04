package com.the.machine.framework.systems.physics;

import box2dLight.ChainLight;
import box2dLight.ConeLight;
import box2dLight.DirectionalLight;
import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.LayerComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.physics.Light2dComponent;
import com.the.machine.framework.components.physics.Light2dRenderComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.physics.Light2dToggleEvent;
import com.the.machine.framework.interfaces.Observable;
import com.the.machine.framework.interfaces.Observer;
import com.the.machine.framework.utility.BitBuilder;
import com.the.machine.framework.utility.EntityUtilities;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/03/15
 */
public class Light2dSystem extends IteratingSystem implements Observer, EntityListener, EventListener {

	private transient ComponentMapper<Light2dComponent> lights = ComponentMapper.getFor(Light2dComponent.class);
	private transient ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	private transient ComponentMapper<Light2dRenderComponent> lightRenderers = ComponentMapper.getFor(Light2dRenderComponent.class);

	private transient RayHandler rayHandler;

	public Light2dSystem() {
		super(Family.all(Light2dComponent.class)
					.get());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(Family.all(Light2dComponent.class)
									   .get(), this);
		if (world.getBox2dWorld() == null) {
			world.setBox2dWorld(new World(new Vector2(0, 0), true));
		}
		Entity lightRenderer = new Entity();
		rayHandler = new RayHandler(world.getBox2dWorld());
		lightRenderer.add(new Light2dRenderComponent().setRayHandler(rayHandler)
													  .setGammaCorrection(true)
													  .setUseDiffuseLights(true)
													  .setAmbientLight(new Color(0.5f, 0.5f, 0.5f, 0.5f))
													  .setBlur(true)
													  .setBlurNum(3));
		lightRenderer.add(new LayerComponent(BitBuilder.none(32)
													   .s(1)
													   .get()));
		world.addEntity(lightRenderer);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
		ImmutableArray<Entity> entities = world.getEntitiesFor(Family.all(Light2dRenderComponent.class)
																	 .get());
		for (Entity entity : entities) {
			world.removeEntity(entity);
		}
	}

	@Override
	public void entityAdded(Entity entity) {
		//		createLight(entity);
		Light2dComponent light2dComponent = lights.get(entity);
		light2dComponent.addObserver(this);
		light2dComponent.forceChanged();
		light2dComponent.notifyObservers();
	}

	@Override
	public void entityRemoved(Entity entity) {
		Light2dComponent light2dComponent = lights.get(entity);
		light2dComponent.deleteObserver(this);
		light2dComponent
				.getLight()
				.remove();
		light2dComponent.getLight()
						.dispose();
		light2dComponent.setLight(null);
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof Light2dToggleEvent) {
			ImmutableArray<Entity> renderers = world.getEntitiesFor(Family.all(Light2dRenderComponent.class)
																		  .get());
			for (Entity renderer : renderers) {
				Light2dRenderComponent renderComponent = lightRenderers.get(renderer);
				renderComponent.setEnabled(!renderComponent.isEnabled());
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		Light2dComponent light2dComponent = (Light2dComponent) o;
		if (light2dComponent.isDirty() || !light2dComponent.getType().getLightClass().isInstance(light2dComponent.getLight())) {
			Entity owner = light2dComponent.getOwner()
											.get();
			if (owner != null) {
				createLight(owner);
			}
			light2dComponent.setDirty(false);
		} else {
			updateLight(light2dComponent);
		}
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		rayHandler.update();
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		Light2dComponent light2dComponent = lights.get(entity);
		light2dComponent.notifyObservers();
		TransformComponent transform = EntityUtilities.computeAbsoluteTransform(entity);
		Light light = light2dComponent.getLight();
		if (light != null) {
			light.setPosition(transform.get2DPosition().scl(0.1f));
			light.setDirection(transform.getZRotation());
		}
	}

	private void updateLight(Light2dComponent lc) {
		Light light = lc.getLight();
		if (light != null) {
			light.setColor(lc.getColor());
			light.setActive(lc.isEnabled());
			light.setDistance(0.2f *lc.getDistance());
			light.setSoft(lc.isSoft());
			light.setSoftnessLength(lc.getSoftnessLength());
			light.setStaticLight(lc.isStaticLight());
			light.setXray(lc.isXray());
			if (light instanceof ConeLight) {
				((ConeLight) light).setConeDegree(lc.getAngle()/2);
			}
			Light.setContactFilter(lc.getFilter());
		}
	}

	private void createLight(Entity entity) {
		Light2dComponent lc = lights.get(entity);
		if (lc.getLight() != null) {
			System.out.println("Disposing light");
			lc.getLight().remove();
			lc.getLight().dispose();
			lc.setLight(null);
		}
		TransformComponent tf = EntityUtilities.computeAbsoluteTransform(entity);
		Light light = null;
		switch (lc.getType()) {
			case POINT:
				light = new PointLight(rayHandler, lc.getRays(), lc.getColor(), 0.2f*lc.getDistance(), tf.getX(), tf.getY());
				break;
			case DIRECTIONAL:
				light = new DirectionalLight(rayHandler, lc.getRays(), lc.getColor(), tf.getZRotation());
				break;
			case CONE:
				light = new ConeLight(rayHandler, lc.getRays(), lc.getColor(), 0.2f *lc.getDistance(), tf.getX(), tf.getY(), tf.getZRotation(), lc.getAngle()/2);
				break;
			case CHAIN:
				light = new ChainLight(rayHandler, lc.getRays(), lc.getColor(), 0.2f *lc.getDistance(), lc.getDirection(), lc.getChain());
				break;
		}
		light.setPosition(tf.get2DPosition());
		light.setSoft(lc.isSoft());
		light.setSoftnessLength(lc.getSoftnessLength());
		light.setStaticLight(lc.isStaticLight());
		light.setXray(lc.isXray());
		Light.setContactFilter(lc.getFilter());
		lc.setLight(light);
	}
}
