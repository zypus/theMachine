package com.the.machine.framework.components;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Bits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 11/02/15
 */
@Data
@Accessors(chain = true)
public class CameraComponent extends AbstractComponent {
	transient private Camera camera = null;
	private Integer clearFlag = new Integer(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
	private Color      background     = Color.BLACK;
	private Bits       cullingMask    = null;
	private Projection projection     = Projection.PERSPECTIVE;
	private Float      zoom           = 1.0f;
	private Float      fieldOfView    = 45f;
	private Clipping   clippingPlanes = new Clipping(0f, 300f);
	private Vector2    origin         = new Vector2(0.5f, 0.5f);
	private Rectangle  viewportRect   = new Rectangle(0, 0, 1, 1);
	private Integer    depth          = new Integer(-1);
	private Texture    targetTexture  = null;

	public CameraComponent setDepth(Integer integer) {
		try {
			Field value = Integer.class.getDeclaredField("value");
			value.setAccessible(true);
			value.set(depth, integer);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		setChanged();
		dirty = true;
		return this;
	}

	@Data
	@AllArgsConstructor
	static public class Clipping {
		private Float near;
		private Float far;
	}

	static public enum Projection {
		PERSPECTIVE,
		ORTHOGRAPHIC
	}

}

