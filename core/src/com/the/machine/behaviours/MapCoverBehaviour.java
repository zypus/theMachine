package com.the.machine.behaviours;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.DiscreteMapComponent;
import com.the.machine.framework.utility.Utils;
import com.the.machine.misc.Placebo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import static com.the.machine.components.AreaComponent.AreaType.*;

/**
 * Simple behaviour which should cover the map in a probabilistic manor.
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 04/05/15
 */
public class MapCoverBehaviour implements BehaviourComponent.Behaviour<MapCoverBehaviour.MapCoverBehaviourState> {

	@Override
	public BehaviourComponent.BehaviourResponse<MapCoverBehaviourState> evaluate(BehaviourComponent.BehaviourContext context, MapCoverBehaviourState state) {
		// if the state is null, that means that this is the first time the behaviour is evaluate, so lets construct the first state
		if (state == null) {
			state = generateState(context);
		}
		updateCoverage(context, state);
		Vector2 centerOfGravitation = determineGlobalCenterOfGravitation(state);
		Vector2 pos = context.getPlacebo()
							 .getPos();
		Vector2 delta = centerOfGravitation.cpy()
										   .sub(pos);
		// determine the rotation to the center of gravity, because we want to go there
		float angle = context.getMoveDirection().angle(delta);
		BehaviourComponent.BehaviourResponse response = new BehaviourComponent.BehaviourResponse<MapCoverBehaviourState>(10, MathUtils.clamp(angle, -44, 44), new ArrayList<>(), state, 0, 0);
		return response;
	}

	private void updateCoverage(BehaviourComponent.BehaviourContext context, MapCoverBehaviourState state) {
		Vector2 pos = context.getPlacebo()
							 .getPos().cpy().add(state.getOffset());
		Vector2 direction = context.getMoveDirection().nor();
		float[][] coverage = state.getCoverage();
		// first lets creap up all values of the coverage back to 1 if there not 1 already
		for (int i = 0; i < coverage.length; i++) {
			for (int j = 0; j < coverage[0].length; j++) {
				// ignores values which are 1, (values which are 2 and should be ignored anyway)
				if (coverage[i][j] < 1) {
					coverage[i][j] += 0.0001f;
					// makes sure the number is never bigger than 1
					if (coverage[i][j] > 1) {
						coverage[i][j] = 1;
					}
				}
			}
		}
		int range = (int) context.getVisionRange();
		// makes sure that range is even, if not decrease the range a bit
		if (range % 2 != 0) {
			range--;
		}
		// determine and update spots that can be seen right now
		int range2 = range * range;
		// check the square created by the vision range around the player
		for (int i = 0; i < range; i++) {
			for (int j = 0; j < range; j++) {
				int dx = i + range/2;
				int dy = j + range/2;
				// check if field is in vision range
				if (dx*dx+dy*dy < range2) {
					Vector2 lookDir = new Vector2(dx, dy).nor();
					float angle = Math.abs(lookDir.angle(direction));
					// check if the vision angle covers that direction
					if (angle < context.getVisionAngle()/2) {
						coverage[(int) pos.x + dx][(int)pos.y + dy] = 0;
					}
				}
			}
		}
	}

	private Vector2 determineGlobalCenterOfGravitation(MapCoverBehaviourState state) {
		int x = 0;
		int y = 0;
		int count = 0;
		float[][] coverage = state.getCoverage();
		for (int i = 0; i < coverage.length; i++) {
			for (int j = 0; j < coverage[0].length; j++) {
				float v = coverage[i][j];
				if (v <= 1) {
					x += v*i;
					y += v*j;
					count++;
				}
			}
		}
		Vector2 offset = state.getOffset();
		return new Vector2((float)x/count - offset.x, (float)y/count - offset.y);
	}

	private MapCoverBehaviourState generateState(BehaviourComponent.BehaviourContext context) {
		MapCoverBehaviourState state = new MapCoverBehaviourState();
		Placebo placebo = context.getPlacebo();
		List<DiscreteMapComponent.MapCell> map = placebo
														.getDiscretizedMap();
		int minX = 10000;
		int minY = 10000;
		int maxX = -10000;
		int maxY = -10000;
		// finds the dimensions of the map
		for (DiscreteMapComponent.MapCell cell : map) {
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
		int width = maxX - minX;
		int height = maxY - minY;
		Vector2 offset = new Vector2(width / 2, height / 2);
		AreaComponent.AreaType[][] area = new AreaComponent.AreaType[width][height];
		// clear the whole area, with the target type for now
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				area[i][j] = TARGET;
			}
		}
		// populate the map with all unwalkable structures
		for (DiscreteMapComponent.MapCell cell : map) {
			Vector2 position = cell.getPosition().cpy().add(offset);
			AreaComponent.AreaType type = cell.getType();
			if (type != OUTER_WALL && type != WALL) {
				boolean inbound = Utils.isInbound(position.x, position.y, 0, 0, width, height);
				if (inbound) {
					area[(int)position.x][(int)position.y] = WALL;
				}
			}
		}
		// perform a flood fill from the current position to determine all reachable areas
		Vector2 myPos = placebo.getPos();
		floodFill(area, (int)(myPos.x+offset.x), (int)(myPos.y+offset.y));
		// construct a coverage map from the obtained map analysis
		float[][] coverage = new float[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (area[i][j] == GROUND) {
					coverage[i][j] = 1.0f; // this is a valid field
				} else {
					coverage[i][j] = 2.0f; // this is an unreachable field
				}
			}
		}
		// store everything in the state
		state.coverage = coverage;
		state.offset = offset;
		return state;
	}

	// orthogonal direction for the flood fill
	private static Vector2[] dirs = new Vector2[]{new Vector2(1, 0), new Vector2(0 , 1), new Vector2(-1, 0), new Vector2(0,-1) };

	private void floodFill(AreaComponent.AreaType[][] area, int x, int y) {
		// if this spot is a target area, mark it as visited
		if (area[x][y] == TARGET) {
			area[x][y] = GROUND;
			// flood continues on the adjacent fields
			for (Vector2 dir : dirs) {
				int nx = (int) (x + dir.x);
				int ny = (int) (y + dir.y);
				if (Utils.isInbound(nx, ny, 0, 0, area.length, area[0].length )) {
					floodFill(area, x, y);
				}
			}
		}
	}

	@NoArgsConstructor
	@Data
	@Accessors(chain = true)
	public static class MapCoverBehaviourState
			implements BehaviourComponent.BehaviourState {
			float[][] coverage;
			Vector2 offset;
	}
}
