package com.the.machine.framework.components.physics;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 10/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ColliderComponent extends AbstractComponent {

	List<Collider> colliders = new ArrayList<>();
	List<Collider> removed = new ArrayList<>();

	public ColliderComponent add(Collider collider) {
		colliders.add(collider);
		return this;
	}

	public ColliderComponent remove(Collider collider) {
		colliders.remove(collider);
		removed.add(collider);
		return this;
	}

	@Getter
	public static class Collider {
		@Setter Fixture fixture;
		float   density = 1;
		Filter  filter = new Filter();
		float   friction = 0;
		boolean sensor = false;
		float   restitution = 0;
		Shape   shape = new CircleShape();

		boolean changed;
		boolean shapeChanged;

		public boolean isChanged() {
			boolean temp = changed;
			changed = false;
			return temp;
		}

		public boolean isShapeChanged() {
			boolean temp = shapeChanged;
			shapeChanged = false;
			return temp;
		}

		public Collider setDensity(float density) {
			if (this.density != density) {
				changed = true;
			}
			this.density = density;
			return this;
		}

		public Collider setFilter(Filter filter) {
			if (!this.filter.equals(filter)) {
				changed = true;
			}
			this.filter = filter;
			return this;
		}

		public Collider setFriction(float friction) {
			if (this.friction != friction) {
				changed = true;
			}
			this.friction = friction;
			return this;
		}

		public Collider setSensor(boolean sensor) {
			if (this.sensor != sensor) {
				changed = true;
			}
			this.sensor = sensor;
			return this;
		}

		public Collider setRestitution(float restitution) {
			if (this.restitution != restitution) {
				changed = true;
			}
			this.restitution = restitution;
			return this;
		}

		public Collider setShape(Shape shape) {
			if (!this.shape.equals(shape)) {
				changed = true;
				shapeChanged = true;
			}
			this.shape = shape;
			return this;
		}

		public Collider setShape(List<Vector2> vector2s) {
			PolygonShape poly = new PolygonShape();
			for (Vector2 vector2 : vector2s) {
				vector2.scl(0.1f);
			}
			poly.set(vector2s.toArray(new Vector2[vector2s.size()]));
			this.shape = poly;
			changed = true;
			shapeChanged = true;
			return this;
		}

		public Collider setShape(Vector2 center, float radius) {
			CircleShape circle = new CircleShape();
			center.scl(0.1f);
			circle.setPosition(center);
			circle.setRadius(radius*0.1f);
			this.shape = circle;
			changed = true;
			shapeChanged = true;
			return this;
		}

		public Collider setShape(Rectangle rectangle) {
			PolygonShape poly = new PolygonShape();
			Vector2 center = new Vector2();
			rectangle.getCenter(center);
			center.scl(0.1f);
			poly.setAsBox(rectangle.getWidth()*0.05f, rectangle.getHeight()*0.05f, center, 0);
			this.shape = poly;
			changed = true;
			shapeChanged = true;
			return this;
		}

		public Collider setShape(float width, float height) {
			PolygonShape poly = new PolygonShape();
			poly.setAsBox(width * 0.05f, height * 0.05f, new Vector2(), 0);
			this.shape = poly;
			changed = true;
			shapeChanged = true;
			return this;
		}

	}

}
