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

}
