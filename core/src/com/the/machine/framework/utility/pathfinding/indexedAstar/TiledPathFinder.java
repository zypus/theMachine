package com.the.machine.framework.utility.pathfinding.indexedAstar;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.the.machine.components.AreaComponent;
import com.the.machine.framework.utility.pathfinding.Vector2i;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/05/15
 */

public class TiledPathFinder {

	public GraphPath<TiledNode> findPath(float[][] map, Vector2i start, Vector2i goal) {

		float max = 0;
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if (max < map[i][j]) {
					max = map[i][j];
				}
			}
		}

		TiledGraph graph = new TiledGraph(map);
		IndexedAStarPathFinder<TiledNode> finder = new IndexedAStarPathFinder<>(graph);

		DefaultGraphPath<TiledNode> path = new DefaultGraphPath<>();

		TiledNode startNode = new TiledNode(start.getX(), start.getY(), map, graph);
		TiledNode goalNode = new TiledNode(goal.getX(), goal.getY(), map, graph);

		graph.add(startNode);
		graph.add(goalNode);

		finder.searchNodePath(startNode, goalNode, new TiledManhattenDistance(max), path);

		return path;
	}

	public static float[][] typeMapToSimpleValueMap(AreaComponent.AreaType[][] area) {
		float[][] map = new float[area.length][area[0].length];
		for (int i = 0; i < area.length; i++) {
			for (int j = 0; j < area[0].length; j++) {
				AreaComponent.AreaType type = area[i][j];
				if (type == AreaComponent.AreaType.WALL || type == AreaComponent.AreaType.OUTER_WALL) {
					map[i][j] = -1;
				} else {
					map[i][j] = 1;
				}
			}
		}
		return map;
	}

}

