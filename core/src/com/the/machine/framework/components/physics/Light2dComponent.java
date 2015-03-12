package com.the.machine.framework.components.physics;

import box2dLight.ChainLight;
import box2dLight.ConeLight;
import box2dLight.DirectionalLight;
import box2dLight.Light;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.the.machine.framework.components.AbstractComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/03/15
 */
@Getter
@NoArgsConstructor
public class Light2dComponent extends AbstractComponent {
	transient @Setter Light light = null;
	LightType type = LightType.POINT;
	float distance = 100;
	Color color = Color.WHITE;
	int rays = 25;
	boolean soft = false;
	float softnessLength = 0f;
	boolean staticLight = false;
	boolean xray = false;

	// only applicable if type = CONE
	float angle = 45;
	// only applicable if type = CHAIN
	int direction = 1;
	float[] chain = new float[]{0};

	@Getter
	@AllArgsConstructor
	public static enum LightType {
		POINT(PointLight.class),
		DIRECTIONAL(DirectionalLight.class),
		CONE(ConeLight.class),
		CHAIN(ChainLight.class);

		private Class<? extends Light> lightClass;
	}

	public Light2dComponent setType(LightType type) {
		if (this.type != type) {
			setChanged();
		}
		this.type = type;
		return this;
	}

	public Light2dComponent setDistance(float distance) {
		if (this.distance != distance) {
			setChanged();
		}
		this.distance = distance;
		return this;
	}

	public Light2dComponent setColor(Color color) {
		if (!this.color.equals(color)) {
			setChanged();
		}
		this.color = color;
		return this;
	}

	public Light2dComponent setRays(int rays) {
		if (this.rays != rays) {
			setChanged();
			dirty = true;
		}
		this.rays = rays;
		return this;
	}

	public Light2dComponent setSoft(boolean soft) {
		if (this.soft != soft) {
			setChanged();
		}
		this.soft = soft;
		return this;
	}

	public Light2dComponent setSoftnessLength(float softnessLength) {
		if (this.softnessLength != softnessLength) {
			setChanged();
		}
		this.softnessLength = softnessLength;
		return this;
	}

	public Light2dComponent setStaticLight(boolean staticLight) {
		if (this.staticLight != staticLight) {
			setChanged();
		}
		this.staticLight = staticLight;
		return this;
	}

	public Light2dComponent setXray(boolean xray) {
		if (this.xray != xray) {
			setChanged();
		}
		this.xray = xray;
		return this;
	}

	public Light2dComponent setAngle(float angle) {
		if (this.angle != angle && type == LightType.CONE) {
			setChanged();
		}
		this.angle = angle;
		return this;
	}

	public Light2dComponent setDirection(int direction) {
		if (this.direction != direction) {
			setChanged();
		}
		this.direction = direction;
		return this;
	}

	public Light2dComponent setChain(float[] chain) {
		if (!Arrays.equals(this.chain, chain) && type == LightType.CHAIN) {
			setChanged();
			dirty = true;
		}
		this.chain = chain;
		return this;
	}
}
