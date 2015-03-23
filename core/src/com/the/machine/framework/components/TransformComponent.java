package com.the.machine.framework.components;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 11/02/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TransformComponent extends ObservableComponent {
	private Vector3 position = new Vector3();
	private Quaternion rotation = new Quaternion();
	private Vector3 scale = new Vector3(1, 1, 1);

	public Vector2 get2DPosition() {
		return new Vector2(position.x, position.y);
	}

	public float getX() {
		return position.x;
	}

	public float getY() {
		return position.y;
	}

	public float getZ() {
		return position.z;
	}

	public float getXRotation() {
		return rotation.getAngleAround(1,0,0);
	}

	public float getYRotation() {
		return rotation.getAngleAround(0,1,0);
	}

	public float getZRotation() {
		return rotation.getAngleAround(0,0,1);
	}

	public float getXScale() {
		return scale.x;
	}

	public float getYScale() {
		return scale.y;
	}

	public float getZScale() {
		return scale.z;
	}

	// SETTER

	public TransformComponent setPosition(Vector3 pos) {
		if (!position.equals(pos)) {
			setChanged();
		}
		position.set(pos);
		return this;
	}

	public TransformComponent setPosition(float x, float y, float z) {
		if (position.x != x || position.y != y || position.z != z) {
			setChanged();
		}
		position.set(x,y,z);
		return this;
	}

	public TransformComponent set2DPosition(Vector2 pos2d) {
		if (position.x != pos2d.x || position.y != pos2d.y) {
			setChanged();
		}
		position.x = pos2d.x;
		position.y = pos2d.y;
		return this;
	}

	public TransformComponent setX(float x) {
		if (position.x != x) {
			setChanged();
		}
		position.x = x;
		return this;
	}

	public TransformComponent setY(float y) {
		if (position.y != y) {
			setChanged();
		}
		position.y = y;
		return this;
	}

	public TransformComponent setZ(float z) {
		if (position.z != z) {
			setChanged();
		}
		position.z = z;
		return this;
	}

	public TransformComponent setRotation(Quaternion rot) {
		if (!rotation.equals(rot)) {
			setChanged();
		}
		rotation.set(rot);
		return this;
	}

	public TransformComponent setRotation(float x, float y, float z) {
		if (getXRotation() != x || getYRotation() != y || getZRotation() != z) {
			setChanged();
		}
		rotation.setEulerAngles(y, x, z);
		return this;
	}

	public TransformComponent setXRotation(float x) {
		if (getXRotation() != x) {
			setChanged();
		}
		rotation.setEulerAngles(getYRotation(), x, getZRotation());
		return this;
	}

	public TransformComponent setYRotation(float y) {
		if (getYRotation() != y) {
			setChanged();
		}
		rotation.setEulerAngles(y, getXRotation(), getZRotation());
		return this;
	}

	public TransformComponent setZRotation(float z) {
		if (getZRotation() != z) {
			setChanged();
		}
		rotation.setEulerAngles(getXRotation(), getYRotation(), z);
		return this;
	}

	public TransformComponent setScale(float allScale) {
		if (scale.x != allScale || scale.y != allScale || scale.z != allScale) {
			setChanged();
		}
		scale.x = allScale;
		scale.y = allScale;
		scale.z = allScale;
		return this;
	}

	public TransformComponent setScale(Vector3 scale) {
		if (!this.scale.equals(scale)) {
			setChanged();
		}
		this.scale.set(scale);
		return this;
	}

	public TransformComponent setScale(float x, float y, float z) {
		if (this.scale.x != x || this.scale.y != y || this.scale.z != z) {
			setChanged();
		}
		this.scale.set(x, y, z);
		return this;
	}

	public TransformComponent setXScale(float x) {
		if (scale.x != x) {
			setChanged();
		}
		scale.x = x;
		return this;
	}

	public TransformComponent setYScale(float y) {
		if (scale.y != y) {
			setChanged();
		}
		scale.y = y;
		return this;
	}

	public TransformComponent setZScale(float z) {
		if (scale.z != z) {
			setChanged();
		}
		scale.z = z;
		return this;
	}

}
