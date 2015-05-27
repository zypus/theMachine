package com.the.machine.framework.utility.pathfinding.indexedAstar;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;
import com.the.machine.framework.utility.Utils;
import javafx.util.Pair;
import lombok.Data;

@Data
public class TiledNode
		extends IndexableNode<TiledNode> {

	int        x;
	int        y;
	float[][]  map;
	TiledGraph graph;

	public TiledNode(int x, int y, float[][] map, TiledGraph graph) {
		this.x = x;
		this.y = y;
		this.map = map;
		this.graph = graph;
	}

	Array<Connection<TiledNode>> conns = null;

	public Array<Connection<TiledNode>> getConnections() {
		if (conns == null) {
			conns = new Array<>();
			for (int d = 0; d < 9; d++) {
				// skip the node itself
				if (d == 4) {
					continue;
				}
				int xDirections = (d % 3) - 1;
				int yDirections = (d / 3) - 1;
				int nx = x + xDirections;
				int ny = y + yDirections;
				if (Utils.isInbound(nx, ny, 0, 0, map.length, map[0].length)) {
					float cost = map[nx][ny];
					TiledNode tiledNode = graph.get(new Pair(nx, ny));
					if (tiledNode != null) {
						TiledConnection connection = new TiledConnection(this, tiledNode, cost);
						conns.add(connection);
					} else if (cost >= 0) {
						TiledNode node = new TiledNode(nx, ny, map, graph);
						graph.add(node);
						TiledConnection connection = new TiledConnection(this, node, cost);
						conns.add(connection);
					}
				}
			}
		}
		return conns;
	}

}
