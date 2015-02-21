package com.the.machine.framework.components;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lombok.Getter;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/02/15
 */
public class DimensionComponent
		extends ObservableComponent {
	private                 Rectangle dimension = new Rectangle();
	@Getter private Vector2   origin = new Vector2(0.5f, 0.5f);
	@Getter private Vector2 pivot = new Vector2(0.5f, 0.5f);

	public float getWidth() {
		return dimension.getWidth();
	}

	public float getHeight() {
		return dimension.getHeight();
	}

	public DimensionComponent setDimension(float width, float height) {
		dimension.setSize(width, height);
		return this;
	}

	public DimensionComponent setWidth(float width) {
		dimension.setWidth(width);
		return this;
	}

	public DimensionComponent setHeight(float height) {
		dimension.setHeight(height);
		return this;
	}

	public float getOriginX() {
		return origin.x;
	}

	public float getOriginY() {
		return origin.y;
	}

	public DimensionComponent setOriginX(float x) {
		origin.x = x;
		return this;
	}

	public DimensionComponent setOriginY(float y) {
		origin.y = y;
		return this;
	}

	public DimensionComponent setOrigin(float x, float y) {
		origin.x = x;
		origin.y = y;
		return this;
	}

	public DimensionComponent setOrigin(Vector2 origin) {
		this.origin = origin;
		return this;
	}

	public float getPivotX() {
		return pivot.x;
	}

	public float getPivotY() {
		return pivot.y;
	}

	public DimensionComponent setPivotX(float x) {
		pivot.x = x;
		return this;
	}

	public DimensionComponent setPivotY(float y) {
		pivot.y = y;
		return this;
	}

	public DimensionComponent setPivot(float x, float y) {
		pivot.x = x;
		pivot.y = y;
		return this;
	}

	public DimensionComponent setPivot(Vector2 pivot) {
		this.pivot = pivot;
		return this;
	}

}
