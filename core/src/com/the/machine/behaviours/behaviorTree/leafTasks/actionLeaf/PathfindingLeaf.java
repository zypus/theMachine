package com.the.machine.behaviours.behaviorTree.leafTasks.actionLeaf;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.behaviours.AstarTestBehaviour;
import com.the.machine.behaviours.AstarTestBehaviour.AstarTestState;
import com.the.machine.behaviours.behaviorTree.TreeContext;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.framework.utility.pathfinding.Vector2i;
import com.the.machine.framework.utility.pathfinding.indexedAstar.TiledNode;
import com.the.machine.framework.utility.pathfinding.indexedAstar.TiledPathFinder;
import com.the.machine.misc.Placebo;
import com.the.machine.misc.SparseToFullMapConverter;
import com.the.machine.systems.ActionSystem;

public class PathfindingLeaf extends LeafTask<TreeContext>{
	
	private TiledPathFinder pathfinder = new TiledPathFinder();
	private AstarState state;
	
	@Override
	public void run(TreeContext context) {
		if(context.getDestination()!=null){
			Placebo placebo = context.getBehaviorContext().getPlacebo();
			// create the state if this is the first call to evaluate
			if (state == null) {
				// compute the fully expressed map from the sparse representation
				int[] characteristics = SparseToFullMapConverter.computeMapCharacteristics(placebo.getDiscretizedMap());
				int width = characteristics[0];
				int height = characteristics[1];
				Vector2 offset = new Vector2(characteristics[2], characteristics[3]);
				AreaComponent.AreaType[][] map = SparseToFullMapConverter.convert(placebo.getDiscretizedMap(), offset, width, height);
				this.state = new AstarState().setMap(TiledPathFinder.typeMapToSimpleValueMap(map))
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

				Vector2i goal = null;//new Vector2i(context.getDestination());
				int counter = 0;
				GraphPath<TiledNode> foundPath = null;
				// try to find the path to a random goal position, if there is non try another
				goal =  new Vector2i(context.getDestination());//new Vector2i(MathUtils.random() * state.getWidth(), MathUtils.random() * state.getHeight());
				foundPath = pathfinder.findPath(state.getMap(), start, goal);
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
			}
			TiledNode node = path.get(0);
			Vector2 delta = new Vector2(node.getX(), node.getY()).sub(offsettedPos);
			float len2 = delta.len2();
			if (len2 <= 1) {
				path.remove(0);
			}
			context.setTargetLocation(delta);
			//System.out.println(delta.x+" "+delta.y);
			this.success();
		}
		
	}

	@Override
	protected Task<TreeContext> copyTo(Task<TreeContext> task) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	@Data
	@NoArgsConstructor
	@Accessors(chain = true)
	public static class AstarState implements BehaviourComponent.BehaviourState {
		float[][] map;
		int width;
		int height;
		Vector2 offset;
		List<TiledNode> path = null;
	}
	
}
