package com.the.machine.framework.utility.pathfinding.indexedAstar;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import javafx.util.Pair;

public class TiledGraph
		implements IndexedGraph<TiledNode> {

	float[][] valueMap;

	IndexedMap<Pair<Integer, Integer>, TiledNode> map = new IndexedMap<>();

	public TiledGraph(float[][] valueMap) {
		this.valueMap = valueMap;
	}

	public Array<Connection<TiledNode>> getConnections(TiledNode fromNode) {
		return map.get(fromNode.getIndex())
				  .getConnections();
	}

	public int getNodeCount() {
		return valueMap.length * valueMap[0].length;
	}

	public TiledNode get(Pair<Integer, Integer> i) {
		return map.get(i);
	}

	public void add(TiledNode node) {
		map.put(new Pair(node.x, node.y), node);
	}

}
