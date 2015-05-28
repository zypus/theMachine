package com.the.machine.map;

import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.framework.utility.Utils;
import com.the.machine.systems.VisionSystem;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to iteratively construct a map.
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/05/15
 */
public class Mapper {
	@Getter List<List<MapTile>> map = new ArrayList<>();
	Vector2 startPosition;
	Vector2 lastPosition;
	@Getter Vector2 currentPosition;
	Vector2 currentDirection;
	@Getter int width  = 0;
	@Getter int height = 0;
	int posXExpansion = 0;
	int negXExpansion = 0;
	int posYExpansion = 0;
	int negYExpansion = 0;

	int posEndX = Integer.MAX_VALUE;
	int negEndX = -Integer.MAX_VALUE;
	int posEndY = Integer.MAX_VALUE;
	int negEndY = -Integer.MAX_VALUE;

	static final AreaComponent.AreaType DEFAULT_TYPE = AreaComponent.AreaType.UNSEEN;

	/**
	 * Call this before you use the mapper
	 * @param startPosition The position you are currently on, for reference purposes only, if you don't knwo where you are specify Vector2(0,0)
	 * @param direction The direction you are currently facing.
	 */
	public void init(Vector2 startPosition, Vector2 direction) {
		this.startPosition = startPosition.cpy();
		lastPosition = startPosition;
		currentPosition = new Vector2(0, 0);
		width = 1;
		height = 1;
		List<MapTile> spot = new ArrayList<>(1);
		spot.add(new MapTile(AreaComponent.AreaType.GROUND));
		map.add(spot);
		currentDirection = direction.cpy()
									.nor();
	}

	/**
	 * Call this each frame, before you use the map.
	 * @param context The current context.
	 */
	public void update(BehaviourComponent.BehaviourContext context) {
		Vector2 direction = context.getMoveDirection();
		float visionAngle = context.getVisionAngle();
		float visionRange = context.getVisionRange();
		List<VisionSystem.EnvironmentVisual > visuals = context.getVision();
		currentPosition.add(context.getPlacebo().getPos().cpy().sub(lastPosition));
		lastPosition = context.getPlacebo().getPos().cpy();
		currentDirection = direction.cpy()
									.nor();
		List<Vector2> sight = inSight(visionRange, visionAngle);
		for (Vector2 p : sight) {
			p.add(currentPosition);
			MapTile tile = get(p.x, p.y);
			if (tile == null || tile.getAreaType() == AreaComponent.AreaType.UNSEEN) {
				set(p.x, p.y, AreaComponent.AreaType.GROUND);
			}
		}
		for (VisionSystem.EnvironmentVisual visual : visuals) {
			Vector2 delta = visual.getDelta();
			Vector2 p = delta.cpy()
							 .add(currentPosition);
			set(p.x, p.y, visual.getType());
		}
	}

	public MapTile[][] toArray() {
		MapTile[][] area = new MapTile[width][height];
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
					if (angle < visionAngle / 2) {
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
		if (Utils.isInbound(x, y, negXExpansion, negYExpansion, width - 1, height - 1)) {
			MapTile tile = map.get(x - negXExpansion)
							  .get(y - negYExpansion);
			tile.setAreaType(type);
		} else {
			if (x < negXExpansion) {
				updateWidth(x, posXExpansion);
			} else if (x > posXExpansion) {
				updateWidth(negXExpansion, x);
			}
			if (y < negYExpansion) {
				updateHeight(y, posYExpansion);
			} else if (y > posYExpansion) {
				updateHeight(negYExpansion, y);
			}
			map.get(x - negXExpansion)
			   .set(y - negYExpansion, new MapTile(type));
		}
	}

	private List<MapTile> makeColumn(int h, AreaComponent.AreaType type) {
		List<MapTile> column = new ArrayList<>(h);
		for (int i = 0; i < h; i++) {
			column.add(new MapTile(type));
		}
		return column;
	}

	private void updateWidth(int newNegX, int newPosX) {
		width = newPosX - newNegX + 1;
		while (newNegX < negXExpansion) {
			List<MapTile> column = makeColumn(height, DEFAULT_TYPE);
			map.add(0, column);
			negXExpansion--;
		}
		while (newPosX > posXExpansion) {
			List<MapTile> column = makeColumn(height, DEFAULT_TYPE);
			map.add(column);
			posXExpansion++;
		}
	}

	private void updateHeight(int newNegY, int newPosY) {
		height = newPosY - newNegY + 1;
		while (newNegY < negYExpansion) {
			for (int x = 0; x < width; x++) {
				map.get(x)
				   .add(0, new MapTile(DEFAULT_TYPE));
			}
			negYExpansion--;
		}
		while (newPosY > posYExpansion) {
			for (int x = 0; x < width; x++) {
				map.get(x)
				   .add(new MapTile(DEFAULT_TYPE));
			}
			posYExpansion++;
		}
	}

	/**
	 * Gets the maptile, based on the current position offset by the specified delta.
	 * @param dx The x offset.
	 * @param dy The y offset.
	 * @return The map tile at that location.
	 */
	public MapTile getFromCurrent(float dx, float dy) {
		return get(currentPosition.x + dx, currentPosition.y + dy);
	}

	/**
	 * Gets the current map tile, uses relative position to the start point.
	 * @param x X relative to the start point.
	 * @param y Y relative to the start point.
	 * @return The map tile at that location, or null if off the map
	 */
	public MapTile get(float x, float y) {
		return get((int)x, (int)y);
	}

	/**
	 * Gets the current map tile, uses relative position to the start point.
	 *
	 * @param x
	 * 		X relative to the start point.
	 * @param y
	 * 		Y relative to the start point.
	 *
	 * @return The map tile at that location, or null if off the map
	 */
	public MapTile get(int x, int y) {
		if (Utils.isInbound(x, y, negXExpansion, negYExpansion, width - 1, height - 1)) {
			return map.get(x - negXExpansion)
					  .get(y - negYExpansion);
		}
		// out of bounds
		return null;
	}

	/**
	 * Gets the current map tile in absolute coordinates, based on the current size of the map
	 *
	 * @param x
	 * 		X relative to the start point.
	 * @param y
	 * 		Y relative to the start point.
	 *
	 * @return The map tile at that location, or null if off the map
	 */
	public MapTile getAbsolute(int x, int y) {
		if (Utils.isInbound(x, y, 0, 0, width - 1, height - 1)) {
			return map.get(x)
					  .get(y);
		}
		// out of bounds
		return null;
	}

	/**
	 * Converts a given position in relative coordinates to absolute map coordinates.
	 * @param pos The position relative to the start point.
	 * @return The position in map coordinates, 0-width, 0-height
	 */
	public Vector2 absolutePos(Vector2 pos) {
		return new Vector2(pos.x - negXExpansion, pos.y - negYExpansion);
	}

	@Data
	public static class MapTile {
		AreaComponent.AreaType areaType;
		// used for the map coverage algorithm
		float                  value = 1;
		boolean insight = false;

		public MapTile(AreaComponent.AreaType type) {
			this.areaType = type;
		}

	}
}
