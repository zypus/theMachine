package com.the.machine.framework.components.physics;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
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
		transient @Setter Fixture fixture;
		float   density = 1;
		Filter  filter = new Filter();
		float   friction = 0;
		boolean sensor = false;
		float   restitution = 0;
		ColliderShape   shape = new CircleCollider(new Vector2(), 10);

		boolean changed      = false;
		boolean shapeChanged = false;

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

		public Collider setShape(ColliderShape shape) {
			if (!this.shape.equals(shape)) {
				changed = true;
				shapeChanged = true;
			}
			this.shape = shape;
			return this;
		}

		public Collider setShape(List<Vector2> vector2s) {
			for (Vector2 vector2 : vector2s) {
				vector2.scl(0.1f);
			}
			this.shape = new PolygonCollider(vector2s);
			changed = true;
			shapeChanged = true;
			return this;
		}

		public Collider setShape(Vector2 center, float radius) {
			center.scl(0.1f);
			this.shape = new CircleCollider(center, radius*0.1f);
			changed = true;
			shapeChanged = true;
			return this;
		}

		public Collider setShape(Rectangle rectangle) {
			Vector2 center = new Vector2();
			rectangle.getCenter(center);
			rectangle.setWidth(rectangle.getWidth() / 10);
			rectangle.setHeight(rectangle.getHeight() / 10);
			rectangle.setCenter(center);
			this.shape = new RectangleCollider(rectangle);
			changed = true;
			shapeChanged = true;
			return this;
		}

		public Collider setShape(float width, float height) {
			this.shape = new RectangleCollider(width/10, height/10);
			changed = true;
			shapeChanged = true;
			return this;
		}

		public Collider setShape(Vector2 first, Vector2 second) {
			first.scl(0.1f);
			second.scl(0.1f);
			this.shape = new EdgeCollider(first, second);
			changed = true;
			shapeChanged = true;
			return this;
		}

		public static interface ColliderShape {

			public Shape createShape();

		}

		public static class CircleCollider implements ColliderShape {

			Vector2 center;
			float radius;

			private CircleCollider(Vector2 center, float radius) {
				this.center = center;
				this.radius = radius;
			}

			@Override
			public Shape createShape() {
				CircleShape circle = new CircleShape();
				circle.setPosition(center);
				circle.setRadius(radius);
				return circle;
			}
		}

		public static class RectangleCollider implements ColliderShape {

			Rectangle rectangle;

			private RectangleCollider(Rectangle rectangle) {
				this.rectangle = rectangle;
			}

			private RectangleCollider(float width, float height) {
				this.rectangle = new Rectangle(-width/2, -height/2, width, height);
			}

			@Override
			public Shape createShape() {
				PolygonShape poly = new PolygonShape();
				Vector2 center = new Vector2();
				rectangle.getCenter(center);
				poly.setAsBox(rectangle.getWidth()/2, rectangle.getHeight()/2, center, 0);
				return poly;
			}
		}

		public static class PolygonCollider implements ColliderShape {

			List<Vector2> vector2s;

			public PolygonCollider(List<Vector2> vector2s) {
				this.vector2s = vector2s;
			}

			@Override
			public Shape createShape() {
				PolygonShape poly = new PolygonShape();
				poly.set(vector2s.toArray(new Vector2[vector2s.size()]));
				return poly;
			}
		}

		public static class EdgeCollider
				implements ColliderShape {

			Vector2 first;
			Vector2 second;

			public EdgeCollider(Vector2 first, Vector2 second) {
				this.first = first;
				this.second = second;
			}

			@Override
			public Shape createShape() {
				EdgeShape edge = new EdgeShape();
				edge.set(first, second);
				return edge;
			}
		}

	}

}
