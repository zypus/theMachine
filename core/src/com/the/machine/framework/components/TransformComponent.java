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
		position.set(pos);
		setChanged();
		return this;
	}

	public TransformComponent setPosition(float x, float y, float z) {
		position.set(x,y,z);
		setChanged();
		return this;
	}

	public TransformComponent set2DPosition(Vector2 pos2d) {
		position.x = pos2d.x;
		position.y = pos2d.y;
		setChanged();
		return this;
	}

	public TransformComponent setX(float x) {
		position.x = x;
		setChanged();
		return this;
	}

	public TransformComponent setY(float y) {
		position.y = y;
		setChanged();
		return this;
	}

	public TransformComponent setZ(float z) {
		position.z = z;
		setChanged();
		return this;
	}

	public TransformComponent setRotation(Quaternion rot) {
		rotation.set(rot);
		setChanged();
		return this;
	}

	public TransformComponent setRotation(float x, float y, float z) {
		rotation.setEulerAngles(-y, x, -z);
		setChanged();
		return this;
	}

	public TransformComponent setXRotation(float x) {
		rotation.setEulerAngles(getYRotation(), x, getZRotation());
		setChanged();
		return this;
	}

	public TransformComponent setYRotation(float y) {
		rotation.setEulerAngles(-y, getXRotation(), getZRotation());
		setChanged();
		return this;
	}

	public TransformComponent setZRotation(float z) {
		rotation.setEulerAngles(getXRotation(), getYRotation(), -z);
		setChanged();
		return this;
	}

	public TransformComponent setScale(float allScale) {
		scale.x = allScale;
		scale.y = allScale;
		scale.z = allScale;
		setChanged();
		return this;
	}

	public TransformComponent setScale(Vector3 scale) {
		this.scale.set(scale);
		setChanged();
		return this;
	}

	public TransformComponent setScale(float x, float y, float z) {
		this.scale.set(x,y,z);
		setChanged();
		return this;
	}

	public TransformComponent setXScale(float x) {
		scale.x = x;
		setChanged();
		return this;
	}

	public TransformComponent setYScale(float y) {
		scale.y = y;
		setChanged();
		return this;
	}

	public TransformComponent setZScale(float z) {
		scale.z = z;
		setChanged();
		return this;
	}

}
