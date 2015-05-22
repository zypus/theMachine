package com.the.machine.map;

import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AreaComponent;
import com.the.machine.framework.utility.Utils;
import com.the.machine.systems.VisionSystem;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/05/15
 */
public class Mapper {
	@Getter List<List<AreaComponent.AreaType>> map = new ArrayList<>();
	Vector2 startPosition;
	Vector2 currentPosition;
	Vector2 currentDirection;
	@Getter int width  = 0;
	@Getter int height = 0;
	int posXExpantion = 0;
	int negXExpantion = 0;
	int posYExpantion = 0;
	int negYExpantion = 0;

	static final AreaComponent.AreaType DEFAULT_TYPE = AreaComponent.AreaType.UNSEEN;

	public void init(Vector2 startPosition, Vector2 direction) {
		this.startPosition = startPosition.cpy();
		currentPosition = new Vector2(0, 0);
		width = 1;
		height = 1;
		List<AreaComponent.AreaType> spot = new ArrayList<>(1);
		spot.add(AreaComponent.AreaType.GROUND);
		map.add(spot);
		currentDirection = direction.cpy()
									.nor();
	}

	public void update(Vector2 direction, float currentSpeed, float deltaTime, float visionAngle, float visionRange, List<VisionSystem.EnvironmentVisual> visuals) {
		currentPosition.add(currentDirection.scl(currentSpeed * deltaTime));
		currentDirection = direction.cpy()
									.nor();
		List<Vector2> sight = inSight(visionRange, visionAngle);
		for (Vector2 p : sight) {
			p.add(currentPosition);
			set(p.x, p.y, AreaComponent.AreaType.GROUND);
		}
		for (VisionSystem.EnvironmentVisual visual : visuals) {
			Vector2 delta = visual.getDelta();
			Vector2 p = delta.cpy()
							 .add(currentPosition);
			set(p.x, p.y, visual.getType());
		}
	}

	public AreaComponent.AreaType[][] toArray() {
		AreaComponent.AreaType[][] area = new AreaComponent.AreaType[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				area[x][y] = map.get(x)
								.get(y);
			}
		}
		return area;
	}

	private List<Vector2> inSight(float visionRange, float visionAngle) {
		List<Vector2> list = new ArrayList<>();
		int range = (int) Math.floor(visionRange);
		float range2 = visionRange * visionRange;
		for (int x = -range; x <= range; x++) {
			for (int y = -range; y <= range; y++) {
				int dist = x * x + y * y;
				if (dist < range2 && dist > 1) {
					Vector2 lookDir = new Vector2(x, y).nor();
					float angle = Math.abs(lookDir.angle(currentDirection));
					// check if the vision angle covers that direction
					if (angle < visionAngle / 4) {
						list.add(new Vector2(x, y));
					}
				}
			}
		}
		return list;
	}

	private void set(float x, float y, AreaComponent.AreaType type) {
		set((int) x, (int) y, type);
	}

	private void set(int x, int y, AreaComponent.AreaType type) {
		if (Utils.isInbound(x, y, negXExpantion, negYExpantion, width - 1, height - 1)) {
			map.get(x - negXExpantion)
			   .set(y - negYExpantion, type);
		} else {
			if (x < negXExpantion) {
				updateWidth(x, posXExpantion);
			} else if (x > posXExpantion) {
				updateWidth(negXExpantion, x);
			}
			if (y < negYExpantion) {
				updateHeight(y, posYExpantion);
			} else if (y > posYExpantion) {
				updateHeight(negYExpantion, y);
			}
			map.get(x - negXExpantion)
			   .set(y - negYExpantion, type);
		}
	}

	private List<AreaComponent.AreaType> makeColumn(int h, AreaComponent.AreaType type) {
		List<AreaComponent.AreaType> column = new ArrayList<>(h);
		for (int i = 0; i < h; i++) {
			column.add(type);
		}
		return column;
	}

	private void updateWidth(int newNegX, int newPosX) {
		width = newPosX - newNegX + 1;
		while (newNegX < negXExpantion) {
			List<AreaComponent.AreaType> column = makeColumn(height, DEFAULT_TYPE);
			map.add(0, column);
			negXExpantion--;
		}
		while (newPosX > posXExpantion) {
			List<AreaComponent.AreaType> column = makeColumn(height, DEFAULT_TYPE);
			map.add(column);
			posXExpantion++;
		}
	}

	private void updateHeight(int newNegY, int newPosY) {
		height = newPosY - newNegY + 1;
		while (newNegY < negYExpantion) {
			for (int x = 0; x < width; x++) {
				map.get(x)
				   .add(0, DEFAULT_TYPE);
			}
			negYExpantion--;
		}
		while (newPosY > posYExpantion) {
			for (int x = 0; x < width; x++) {
				map.get(x)
				   .add(DEFAULT_TYPE);
			}
			posYExpantion++;
		}
	}

	public AreaComponent.AreaType get(float x, float y) {
		return get((int)x, (int)y);
	}

	public AreaComponent.AreaType get(int x, int y) {
		if (Utils.isInbound(x, y, negXExpantion, negYExpantion, width - 1, height - 1)) {
			return map.get(x - negXExpantion)
					  .get(y - negYExpantion);
		}
		// out of bounds
		return null;
	}
}
