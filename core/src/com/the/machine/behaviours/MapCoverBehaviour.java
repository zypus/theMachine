package com.the.machine.behaviours;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.Constants;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.DiscreteMapComponent;
import com.the.machine.debug.MapDebugWindow;
import com.the.machine.debug.MapperDebugWindow;
import com.the.machine.debug.ValueMapDebugger;
import com.the.machine.framework.utility.Utils;
import com.the.machine.framework.utility.pathfinding.Vector2i;
import com.the.machine.framework.utility.pathfinding.indexedAstar.TiledNode;
import com.the.machine.framework.utility.pathfinding.indexedAstar.TiledPathFinder;
import com.the.machine.map.Mapper;
import com.the.machine.misc.Placebo;
import com.the.machine.systems.ActionSystem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.the.machine.components.AreaComponent.AreaType.*;

/**
 * Simple behaviour which should cover the map in a probabilistic manor.
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 04/05/15
 */
public class MapCoverBehaviour
		implements BehaviourComponent.Behaviour<MapCoverBehaviour.MapCoverBehaviourState> {

	static final float ALPHA = 0.7f;
	static final float BETA = 0.6f;
	static final float GAMMA = 0.4f;

	static final float DELTA_TIME = 20;

	static private MapCoverBehaviourState sharedState = null;
	static private int                    shareCount  = 0;

	Mapper mapper = new Mapper();

	List<AgentSighting> agentSightings = new ArrayList<>();
	TiledPathFinder pathfinder = new TiledPathFinder();

	@Override
	public List<BehaviourComponent.BehaviourResponse> evaluate(BehaviourComponent.BehaviourContext context, MapCoverBehaviourState state) {
		// shared state between all guard agents, EXPERIMENTAL
		if (state == null) {
			shareCount++;
		}
		if (sharedState != null) {
			state = sharedState;
		}
		// if the state is null, that means that this is the first time the behaviour is evaluate, so lets construct the first state
		if (state == null) {
			state = generateState(context);
			//			sharedState = state; // comment this line to disable shared state
			MapDebugWindow.debug(state.coverage, 0.5f);
			// initialize the map builder
			mapper.init(context.getPlacebo().getPos(), context.getMoveDirection());
			MapperDebugWindow.debug(mapper, 1f);
		}
		// update the map builder
		mapper.update(context);
		updateCoverage(context, state);
		updateCoverage2(context, state);
		//		Vector2 goal = determineGlobalCenterOfGravitation(context, state);
		//		Vector2 goal = closestMax(context, state, 10);
		//		Vector2 pos = context.getPlacebo()
		//							 .getPos();
		//		Vector2 delta = goal.cpy()
		//							.sub((int) pos.x, (int) pos.y);

		Vector2 delta = executePolicy(context, state);

		// determine the rotation to the center of gravity, because we want to go there
		float angle = context.getMoveDirection()
							 .angle(delta);
		List<BehaviourComponent.BehaviourResponse> responses = new ArrayList<>();
		responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.MOVE, new ActionSystem.MoveData(10)));
		responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.TURN, new ActionSystem.TurnData(delta, 45)));
		responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.STATE, new ActionSystem.StateData(state)));
		return responses;
	}

	private void updateCoverage2(BehaviourComponent.BehaviourContext context, MapCoverBehaviourState state) {
		int range = (int) context.getVisionRange();
		// makes sure that range is even, if not decrease the range a bit
		if (range % 2 != 0) {
			range--;
		}
		// determine and update spots that can be seen right now
		int range2 = range * range;
		Vector2 direction = context.getMoveDirection()
								   .nor();
		// check the square created by the vision range around the player
		for (int i = 0; i < range; i++) {
			for (int j = 0; j < range; j++) {
				int dx = i - range / 2;
				int dy = j - range / 2;
				// check if field is in vision range
				int dist = dx * dx + dy * dy;
				if (dist < range2 && dist > 1) {
					Vector2 lookDir = new Vector2(dx, dy).nor();
					float angle = Math.abs(lookDir.angle(direction));
					// check if the vision angle covers that direction
					Mapper.MapTile tile = mapper.getFromCurrent(dx, dy);
					if (tile != null) {
						if (angle < context.getVisionAngle() / 2) {
							if (!tile.isInsight()) {
								tile.setInsight(true);
								tile.setValue(Math.max(tile.getValue() - 0.2f, 0));
							}
							AreaComponent.AreaType type = tile.getAreaType();
							if (type == WALL || type == OUTER_WALL) {
								tile.setValue(0);
							}
						} else {
							tile.setInsight(false);
						}
					}
				}
			}
		}
	}

	private Vector2 executePolicy(BehaviourComponent.BehaviourContext context, MapCoverBehaviourState state) {
		Vector2 current = mapper.absolutePos(mapper.getCurrentPosition());
		// create value maps
		float[][] currentSituation = currentSituation();
		float[][] futureSituation = futureSituation();
		float[][] direction = direction(context.getMoveDirection());
		float[][] reachable = reachable();
		float[][] walkable = walkable();

		// final map
		float[][] valueMap = new float[mapper.getWidth()][mapper.getHeight()];
		float max = 0;
		Vector2 maxPos = new Vector2();
		for (int x = 0; x < mapper.getWidth(); x++) {
			for (int y = 0; y < mapper.getHeight(); y++) {
				valueMap[x][y] = currentSituation[x][y] + ALPHA * futureSituation[x][y] + BETA * direction[x][y] + GAMMA * reachable[x][y];
				if (walkable[x][y] == -1) {
					valueMap[x][y] = 0;
				}
				if (valueMap[x][y] > max) {
					max = valueMap[x][y];
					maxPos.set(x, y);
				}
			}
		}

//		float[][] navMap = new float[mapper.getWidth()][mapper.getHeight()];
//		for (int x = 0; x < mapper.getWidth(); x++) {
//			for (int y = 0; y < mapper.getHeight(); y++) {
//				if (walkable[x][y] == -1) {
//					navMap[x][y] = -1;
//				} else {
//					navMap[x][y] = max-valueMap[x][y];
//				}
//			}
//		}

		// path find
		Vector2i start = new Vector2i(current.x, current.y);

		Vector2i goal = new Vector2i(maxPos.x, maxPos.y);
		GraphPath<TiledNode> path = pathfinder.findPath(walkable, start, goal);

		List<Vector2> points = new ArrayList<>();
		if (path != null) {
			for (TiledNode node : path) {
				points.add(new Vector2(node.getX(), node.getY()));
			}
		}

		ValueMapDebugger.debug(valueMap, points);

		if (path == null || path.getCount() <= 1 || path.getCount() > 2*DELTA_TIME || valueMap[((int) current.x)][((int) current.y)] == -1) {
			return new Vector2(MathUtils.random(-1,1),MathUtils.random(-1,1));
		} else {
			Vector2 next = new Vector2(path.get(1)
										.getX(), path.get(1)
													 .getY());
//			current.x = Math.round(current.x);
//			current.y = Math.round(current.y);
			Vector2 dir = next.sub(current)
							  .nor();
			return dir;
		}
	}

	private float[][] currentSituation() {
		float[][] valueMap = new float[mapper.getWidth()][mapper.getHeight()];
		for (int x = 0; x < mapper.getWidth(); x++) {
			for (int y = 0; y < mapper.getHeight(); y++) {
				valueMap[x][y] = mapper.getMap().get(x).get(y).getValue();
			}
		}
		return valueMap;
	}

	private float[][] futureSituation() {
		float[][] valueMap = new float[mapper.getWidth()][mapper.getHeight()];
		for (int x = 0; x < mapper.getWidth(); x++) {
			for (int y = 0; y < mapper.getHeight(); y++) {
				valueMap[x][y] = mapper.getMap()
									   .get(x)
									   .get(y)
									   .getValue();
			}
		}
		int delta = (int) (DELTA_TIME * Constants.AVERAGE_INTRUDER_SPEED);
		int delta2 = (int) Math.pow(delta, 2);
		for (AgentSighting sighting : agentSightings) {
			int x = (int) sighting.getLocation().x;
			int y = (int) sighting.getLocation().y;
			for (int dx = -delta; dx < delta; dx++) {
				for (int dy = -delta; dy < delta; dy++) {
					int dist = dx * dx + dy * dy;
					if (dist < delta2) {
						Mapper.MapTile tile = mapper.getAbsolute(x+dx,y+dy);
						if (tile != null) {
							Vector2 absolutePos = mapper.absolutePos(new Vector2(x + dx, y + dy));
							valueMap[((int) absolutePos.x)][((int) absolutePos.y)] += 1f - (float)dist / (float)delta2;
						}
					}
				}
			}
		}
		return valueMap;
	}

	private float[][] direction(Vector2 ref) {
		float[][] valueMap = new float[mapper.getWidth()][mapper.getHeight()];
		Vector2 currentPos = mapper.absolutePos(mapper.getCurrentPosition());
		int delta2 = (int) Math.pow(DELTA_TIME * Constants.AGENT_SPEED, 2);
		for (int x = 0; x < mapper.getWidth(); x++) {
			for (int y = 0; y < mapper.getHeight(); y++) {
				Vector2 delta = currentPos.cpy()
										   .sub(new Vector2(x, y));
				float angle = delta.angle(ref);
				valueMap[x][y] = Math.abs(angle)/180f;
			}
		}
		return valueMap;
	}

	private float[][] reachable() {
		float[][] valueMap = new float[mapper.getWidth()][mapper.getHeight()];
		Vector2 currentPos = mapper.absolutePos(mapper.getCurrentPosition());
		int delta= (int) (DELTA_TIME * Constants.AGENT_SPEED);
		float[][] walkable = walkable();
		List<Vector2> next = new ArrayList<>();
		next.add(currentPos);
		while (!next.isEmpty()) {
			floodFill(walkable, next);
		}
		for (int x = 0; x < mapper.getWidth(); x++) {
			for (int y = 0; y < mapper.getHeight(); y++) {
				int dist = (int) currentPos.cpy()
										   .sub(new Vector2(x, y))
										   .len();
				valueMap[x][y] = (dist > delta || walkable[x][y] != 0)
								 ? 0f
								 : (float)dist / (float)delta;
			}
		}
		return valueMap;
	}

	private float[][] walkable() {
		float[][] valueMap = new float[mapper.getWidth()][mapper.getHeight()];
		for (int x = 0; x < mapper.getWidth(); x++) {
			for (int y = 0; y < mapper.getHeight(); y++) {
				Mapper.MapTile tile = mapper.getMap()
											.get(x)
											.get(y);
				Mapper.MapTile right = mapper.getAbsolute(x + 1, y);
				Mapper.MapTile up = mapper.getAbsolute(x, y+1);
				Mapper.MapTile left = mapper.getAbsolute(x - 1, y);
				Mapper.MapTile down = mapper.getAbsolute(x, y-1);
				Mapper.MapTile upright = mapper.getAbsolute(x+1, y+1);
				Mapper.MapTile upleft = mapper.getAbsolute(x-1, y+1);
				Mapper.MapTile downright = mapper.getAbsolute(x+1, y-1);
				Mapper.MapTile downleft = mapper.getAbsolute(x-1, y-1);
				if (tile.getAreaType() == WALL || tile.getAreaType() == OUTER_WALL ||
												  (right != null && (right.getAreaType().isWall())) ||
												  (up != null && (up.getAreaType()
																		  .isWall())) ||
												  (left != null && (left.getAreaType()
																		  .isWall())) ||
					(upright != null && (upright.getAreaType()
																		  .isWall())) ||
					(upleft != null && (upleft.getAreaType()
																		  .isWall())) ||
					(downright != null && (downright.getAreaType()
																		  .isWall())) ||
					(downleft != null && (downleft.getAreaType()
																		  .isWall())) ||
												  (down != null && (down.getAreaType()
																		  .isWall()))) {
					valueMap[x][y] = -1;
				} else {
					valueMap[x][y] = 1;
				}
			}
		}
		return valueMap;
	}

	private void updateCoverage(BehaviourComponent.BehaviourContext context, MapCoverBehaviourState state) {
		Vector2 pos = context.getPlacebo()
							 .getPos()
							 .cpy()
							 .add(state.getOffset());
		Vector2 direction = context.getMoveDirection()
								   .nor();
		float[][] coverage = state.getCoverage();
		// first lets creap up all values of the coverage back to 1 if there not 1 already
		for (int i = 0; i < coverage.length; i++) {
			for (int j = 0; j < coverage[0].length; j++) {
				// ignores values which are 1, (values which are 2 and should be ignored anyway)
				if (coverage[i][j] >= 0) {
					coverage[i][j] += 0.001f * context.getPastTime() * ((sharedState != null)
																		? 1f / shareCount
																		: 1); // compensate for additional agents
					// makes sure the number is never bigger than 1
					//					if (coverage[i][j] > 1) {
					//						coverage[i][j] = 1;
					//					}
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
				int dx = i - range / 2;
				int dy = j - range / 2;
				// check if field is in vision range
				if (dx * dx + dy * dy < range2) {
					Vector2 lookDir = new Vector2(dx, dy).nor();
					float angle = Math.abs(lookDir.angle(direction));
					// check if the vision angle covers that direction
					if (angle < context.getVisionAngle() / 2) {
						int xx = (int) pos.x + dx;
						int yy = (int) pos.y + dy;
						if (Utils.isInbound(xx, yy, 0, 0, coverage.length - 1, coverage[0].length - 1)) {
							if (coverage[xx][yy] >= 0) {
								coverage[xx][(yy)] = 0;
							}
						}
					}
				}
			}
		}
	}

	private Vector2 closestMax(BehaviourComponent.BehaviourContext context, MapCoverBehaviourState state, int radius) {
		float max = 0;
		List<Vector2> tie = new ArrayList<>();
		float[][] coverage = state.getCoverage();
		Vector2 pos = context.getPlacebo()
							 .getPos()
							 .cpy()
							 .add(state.getOffset());
		for (int i = Math.max(0, (int) pos.x - radius); i < Math.min((int) pos.x + radius, coverage.length); i++) {
			for (int j = Math.max(0, (int) pos.y - radius); j < Math.min((int) pos.y + radius, coverage[0].length); j++) {
				int manhatten = Math.abs(i - (int) pos.x) + Math.abs(j - (int) pos.y);
				float v = coverage[i][j] - manhatten;
				if (v > max) {
					max = v;
					tie.clear();
					tie.add(new Vector2(i, j));
				} else if (v == max) {
					tie.add(new Vector2(i, j));
				}
			}
		}
		Vector2 offset = state.getOffset();
		Collections.shuffle(tie);
		return tie.get(0)
				  .sub(offset.x, offset.y);
	}

	private Vector2 determineGlobalCenterOfGravitation(BehaviourComponent.BehaviourContext context, MapCoverBehaviourState state) {
		float x = 0;
		float y = 0;
		float count = 0;
		float[][] coverage = state.getCoverage();
		Vector2 pos = context.getPlacebo()
							 .getPos()
							 .cpy()
							 .add(state.getOffset());
		for (int i = 0; i < coverage.length; i++) {
			for (int j = 0; j < coverage[0].length; j++) {
				// weight the power of attraction based on manhattan distance to bot
				float distWeight = MathUtils.clamp(1 - ((float) Math.pow(Math.abs(pos.x - i) + Math.abs(pos.y - j), 2f) / (coverage.length + coverage.length)), 0, 1);
				float v = coverage[i][j] * distWeight;
				if (v >= 0) {
					x += v * i;
					y += v * j;
					count += v;
				}
			}
		}
		Vector2 offset = state.getOffset();
		return new Vector2(x / count - offset.x, y / count - offset.y);
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
			Vector2 position = cell.getPosition()
								   .cpy()
								   .add(offset);
			AreaComponent.AreaType type = cell.getType();
			if (type == OUTER_WALL || type == WALL) {
				boolean inbound = Utils.isInbound(position.x, position.y, 0, 0, width - 1, height - 1);
				if (inbound) {
					area[(int) position.x][(int) position.y] = WALL;
				}
			}
		}
		// perform a flood fill from the current position to determine all reachable areas
		Vector2 myPos = placebo.getPos();
		List<Vector2> next = new ArrayList<>();
		next.add(myPos.cpy()
					  .add(offset));
		while (!next.isEmpty()) {
			floodFill(area, next);
		}
		// construct a coverage map from the obtained map analysis
		float[][] coverage = new float[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (area[i][j] == GROUND) {
					coverage[i][j] = 1.0f; // this is a valid field
				} else {
					coverage[i][j] = -1.0f; // this is an unreachable field
				}
			}
		}
		// store everything in the state
		state.coverage = coverage;
		state.offset = offset;
		return state;
	}

	// orthogonal direction for the flood fill
	private static Vector2[] dirs = new Vector2[] { new Vector2(1, 0), new Vector2(0, 1), new Vector2(-1, 0), new Vector2(0, -1) };

	private void floodFill(AreaComponent.AreaType[][] area, List<Vector2> next) {
		int x, y;
		if (!next.isEmpty()) {
			Vector2 vector2 = next.remove(0);
			x = (int) vector2.x;
			y = (int) vector2.y;
			// if this spot is a target area, mark it as visited
			if (area[x][y] == TARGET) {
				area[x][y] = GROUND;
				// flood continues on the adjacent fields
				for (Vector2 dir : dirs) {
					int nx = (int) (x + dir.x);
					int ny = (int) (y + dir.y);
					if (Utils.isInbound(nx, ny, 0, 0, area.length - 1, area[0].length - 1)) {
						next.add(new Vector2(nx, ny));
					}
				}
			}
		}
	}

	private void floodFill(float[][] area, List<Vector2> next) {
		int x, y;
		if (!next.isEmpty()) {
			Vector2 vector2 = next.remove(0);
			x = (int) vector2.x;
			y = (int) vector2.y;
			// if this spot is a target area, mark it as visited
			if (area[x][y] == 1) {
				area[x][y] = 0;
				// flood continues on the adjacent fields
				for (Vector2 dir : dirs) {
					int nx = (int) (x + dir.x);
					int ny = (int) (y + dir.y);
					if (Utils.isInbound(nx, ny, 0, 0, area.length - 1, area[0].length - 1)) {
						next.add(new Vector2(nx, ny));
					}
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
		Vector2   offset;
	}

	@AllArgsConstructor
	@Data
	public static class AgentSighting {
		Vector2 location;
		float   time;
	}
}
