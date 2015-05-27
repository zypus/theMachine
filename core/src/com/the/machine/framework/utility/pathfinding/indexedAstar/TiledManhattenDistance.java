package com.the.machine.framework.utility.pathfinding.indexedAstar;

import com.badlogic.gdx.ai.pfa.Heuristic;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TiledManhattenDistance
		implements Heuristic<TiledNode> {

	float maxCost;

	@Override
	public float estimate(TiledNode node, TiledNode endNode) {
		return (Math.abs(node.x - endNode.x) + Math.abs(node.y - endNode.y)) * maxCost;
	}

}
