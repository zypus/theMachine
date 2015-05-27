package com.the.machine.framework.utility.pathfinding.indexedAstar;

import com.badlogic.gdx.ai.pfa.Heuristic;
import lombok.AllArgsConstructor;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/05/15
 */
@AllArgsConstructor
public class TiledEuclideanDistance
		implements Heuristic<TiledNode> {

	float maxCost;

	@Override
	public float estimate(TiledNode node, TiledNode endNode) {
		return (float) (Math.sqrt(Math.pow(node.x - endNode.x, 2) + Math.pow(node.y - endNode.y, 2)) * maxCost);
	}
}
