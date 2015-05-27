package com.the.machine.misc;

import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.DiscreteMapComponent;

import java.util.List;

/**
 * Takes in a sparse map and converts it to a fully expressed version, and/or updates it.
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 13/05/15
 */
public class SparseToFullMapConverter {

	/**
	 * Converts a sparse mapcell map into a fully expressed 2d areatype map. Computes the relevant informations from the sparse map.
	 * @param sparseMap The sparse map.
	 * @return The fully expressed map.
	 */
	public static AreaComponent.AreaType[][] convert(List<DiscreteMapComponent.MapCell> sparseMap) {
		int[] mapStats = computeMapCharacteristics(sparseMap);
		return convert(sparseMap, new Vector2(mapStats[2], mapStats[3]), mapStats[0], mapStats[1]);
	}

	/**
	 * Converts a sparse mapcell map into a fully expressed 2d areatype map.
	 * @param sparseMap The sparse map.
	 * @param offset The offset, vector from the lower left point of the sparse map to its center. Required because an array used positive indices but the sparse maps center is at the origin, hence there are negative coordinates.
	 * @param width Total width of the map.
	 * @param height Total height of the map.
	 * @return The fully expressed map.
	 */
	public static AreaComponent.AreaType[][] convert(List<DiscreteMapComponent.MapCell> sparseMap, Vector2 offset, int width, int height) {
		AreaComponent.AreaType[][] map = new AreaComponent.AreaType[width][height];
		// first clear the whole map, aka set everything to be ground
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				map[i][j] = AreaComponent.AreaType.GROUND;
				if (i == 0 || j == 0 || i == width-1 || j == height-1) {
					map[i][j] = AreaComponent.AreaType.OUTER_WALL;
				}
			}
		}
		update(map, sparseMap, offset);
		return map;
	}

	/**
	 * Takes in a fully expressed map and the sparse map representation and updates the sparse map accordingly.
	 * @param map The fully expressed map which will be updated in place.
	 * @param sparseMap The sparse map which is the source of the update.
	 * @param offset The offset needed to shift the sparse map coordinates. Reason @see{convert}.
	 */
	public static void update(AreaComponent.AreaType[][] map, List<DiscreteMapComponent.MapCell> sparseMap, Vector2 offset) {
		// iterate over all elements of the sparse map and set the area type accordingly in the complete map
		for (DiscreteMapComponent.MapCell cell : sparseMap) {
			int x = (int) cell.getPosition().x + (int) offset.x;
			int y = (int) cell.getPosition().y + (int) offset.y;
			map[x][y] = cell.getType();
		}
	}

	/**
	 * Computes the width, height and the offset from the lower left corner to the center of the sparse map.
	 * @param sparseMap The sparse map.
	 * @return A int array containing 4 elements which encodes the following {width, height, offset.x, offset.y}.
	 */
	public static int[] computeMapCharacteristics(List<DiscreteMapComponent.MapCell> sparseMap) {
		int minX = 10000;
		int minY = 10000;
		int maxX = -10000;
		int maxY = -10000;
		// finds the dimensions of the map
		for (DiscreteMapComponent.MapCell cell : sparseMap) {
			Vector2 position = cell.getPosition();
			if (minX > position.x) {
				minX = (int) position.x;
			}
			if (maxX < position.x) {
				maxX = (int) position.x;
			}
			if (minY > position.y) {
				minY = (int) position.y;
			}
			if (maxY < position.y) {
				maxY = (int) position.y;
			}
		}
		int width = maxX - minX + 2; // +2 because of the outer walls
		int height = maxY - minY + 2;
		return new int[] {width, height, width/2, height/2};
	}

}
