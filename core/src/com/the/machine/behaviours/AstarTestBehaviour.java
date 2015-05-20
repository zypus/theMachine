package com.the.machine.behaviours;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.framework.utility.pathfinding.Vector2i;
import com.the.machine.framework.utility.pathfinding.indexedAstar.TiledNode;
import com.the.machine.framework.utility.pathfinding.indexedAstar.TiledPathFinder;
import com.the.machine.misc.Placebo;
import com.the.machine.misc.SparseToFullMapConverter;
import com.the.machine.systems.ActionSystem;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple astar behaviour that tries to move to a random location, one its reached that location its tries to move to another location.
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 13/05/15
 */
@NoArgsConstructor
public class AstarTestBehaviour implements BehaviourComponent.Behaviour<AstarTestBehaviour.AstarTestState> {

	TiledPathFinder pathfinder = new TiledPathFinder();

	@Override
	public List<BehaviourComponent.BehaviourResponse> evaluate(BehaviourComponent.BehaviourContext context, AstarTestState state) {
		Placebo placebo = context.getPlacebo();
		// create the state if this is the first call to evaluate
		if (state == null) {
			// compute the fully expressed map from the sparse representation
			int[] characteristics = SparseToFullMapConverter.computeMapCharacteristics(placebo.getDiscretizedMap());
			int width = characteristics[0];
			int height = characteristics[1];
			Vector2 offset = new Vector2(characteristics[2], characteristics[3]);
			AreaComponent.AreaType[][] map = SparseToFullMapConverter.convert(placebo.getDiscretizedMap(), offset, width, height);
			state = new AstarTestState().setMap(TiledPathFinder.typeMapToSimpleValueMap(map))
										.setWidth(width)
										.setHeight(height)
										.setOffset(offset);
		}
		List<TiledNode> path = state.getPath();
		Vector2 offset = state.getOffset();
		Vector2 offsettedPos = placebo.getPos()
									  .cpy()
									  .add(offset);
		// if the current path the agents follows is not yet created or empty to the goal was reached, create a new path.
		if (path == null || path.isEmpty()) {
			// the current position will be the start position of the astar
			Vector2i start = new Vector2i(offsettedPos.x, offsettedPos.y);

			Vector2i goal = null;
			int counter = 0;
			GraphPath<TiledNode> foundPath = null;
			// try to find the path to a random goal position, if there is non try another
			while (foundPath == null || foundPath.getCount() == 0) {
				goal = new Vector2i(MathUtils.random() * state.getWidth(), MathUtils.random() * state.getHeight());
				foundPath = pathfinder.findPath(state.getMap(), start, goal);
				counter++;
			}
			if (path == null) {
				path = new ArrayList<>(foundPath.getCount());
			} else {
				path.clear();
			}
			for (TiledNode tn : foundPath) {
				path.add(tn);
			}
			System.out.println(path.get(path.size()-1));
			state.setPath(path);
//			System.out.println("Start: " + start.getX() + ";" + start.getY());
//			System.out.println("Goal: " + goal.getX() + ";" + goal.getY());
//			for (Node node : path) {
//				System.out.print("(" + node.tile.getX() + ";" + node.tile.getY() + ") <- ");
//			}
//			System.out.println();
		}
		TiledNode node = path.get(0);
		Vector2 delta = new Vector2(node.getX(), node.getY()).sub(offsettedPos);
		float len2 = delta.len2();
		if (len2 <= 0.5) {
			path.remove(0);
		}

		List<BehaviourComponent.BehaviourResponse> responses = new ArrayList<>();
		responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.STATE, new ActionSystem.StateData(state)));
		responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.MOVE, new ActionSystem.MoveData(path.isEmpty() ? 0 : 4)));
		responses.add(new BehaviourComponent.BehaviourResponse(ActionSystem.Action.TURN, new ActionSystem.TurnData(delta, 180)));

		return responses;
	}

	@Data
	@NoArgsConstructor
	@Accessors(chain = true)
	public static class AstarTestState
			implements BehaviourComponent.BehaviourState {
		float[][] map;
		int width;
		int height;
		Vector2 offset;
		List<TiledNode> path = null;
	}
}
