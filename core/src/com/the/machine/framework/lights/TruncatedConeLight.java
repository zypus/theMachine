package com.the.machine.framework.lights;

import box2dLight.ConeLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import lombok.Getter;
import lombok.Setter;

/**
 * FIXME
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/03/15
 */
public class TruncatedConeLight extends ConeLight {

	@Getter @Setter protected float minDistance;
	@Getter @Setter protected float maxDistance;

	/**
	 * Creates new positional light and automatically adds it to the specified
	 * {@link box2dLight.RayHandler} instance.
	 *
	 * @param rayHandler
	 * 		not null instance of RayHandler
	 * @param rays
	 * 		number of rays - more rays make light to look more realistic
	 * 		but will decrease performance, can't be less than MIN_RAYS
	 * @param color
	 * 		light color
	 * @param distance
	 * 		light distance (if applicable)
	 * @param x
	 * 		horizontal position in world coordinates
	 * @param y
	 * 		vertical position in world coordinates
	 * @param directionDegree
	 */
	public TruncatedConeLight(RayHandler rayHandler, int rays, Color color, float distance, float x, float y, float directionDegree, float coneDegree, float min, float max) {
		super(rayHandler, rays, color, distance, x, y, directionDegree, coneDegree);
		minDistance = min;
		maxDistance = max;
	}

	@Override
	public void setDistance(float dist) {
		maxDistance = dist;
		minDistance = 0;
		dirty = true;
	}

	@Override
	public boolean contains(float x, float y) {
		// fast fail
		final float x_d = start.x - x;
		final float y_d = start.y - y;
		final float dst2 = x_d * x_d + y_d * y_d;
		if (maxDistance * maxDistance <= dst2 || minDistance * minDistance >= dst2)
			return false;

		// actual check
		boolean oddNodes = false;
		float x2 = mx[rayNum] = start.x;
		float y2 = my[rayNum] = start.y;
		float x1, y1;
		for (int i = 0; i <= rayNum; x2 = x1, y2 = y1, ++i) {
			x1 = mx[i];
			y1 = my[i];
			if (((y1 < y) && (y2 >= y)) || (y1 >= y) && (y2 < y)) {
				if ((y - y1) / (y2 - y1) * (x2 - x1) < (x - x1))
					oddNodes = !oddNodes;
			}
		}
		return oddNodes;
	}

	@Override
	protected void updateMesh() {
//		for (int i = 0; i < rayNum; i++) {
//			m_index = i;
//			f[i] = 1f;
//			tmpEnd.x = endX[i] + start.x;
//			mx[i] = tmpEnd.x;
//			tmpEnd.y = endY[i] + start.y;
//			my[i] = tmpEnd.y;
//			if (rayHandler.world != null && !xray) {
//				rayHandler.world.rayCast(ray, start, tmpEnd);
//			}
//		}
//		setMesh();
	}
}
