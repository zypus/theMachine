package com.the.machine.framework.utility.pathfinding;

//package com.the.machine.components;
//import com.the.machine.components.DiscreteMapComponent;
//import com.badlogic.gdx.math.Vector2;

import com.the.machine.components.AreaComponent;
import com.the.machine.framework.utility.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.the.machine.components.AreaComponent.AreaType.*;

//import static com.the.machine.components.AreaComponent.AreaType.*;

public class AstarPathfinder {

	public List<Node> findPath(AreaComponent.AreaType[][] area, Vector2i start, Vector2i goal) {

		List<Node> openList = new ArrayList<Node>();
		List<Node> closedList = new ArrayList<Node>();

		//start location of the open list, and empty the closed list
		Node current = new Node(start, null, 0, getDistance(start, goal));
		openList.add(current);

		while (openList.size() > 0) {
			Collections.sort(openList, nodeSorter);
			current = openList.get(0);

			if (current.tile.equals(goal)) {
				ArrayList<Node> path = new ArrayList<Node>();
				while (current.parent != null) {
					path.add(current);
					current = current.parent;
				}
				openList.clear();
				closedList.clear();
				return path;
			}

			openList.remove(current);
			closedList.add(current);

			for (int i = 0; i < 9; i++) { // adjecent node
				if (i == 4) {
					continue;  // middle node
				}
				int x = current.tile.getX();
				int y = current.tile.getY();
				int xDirections = (i % 3) - 1;
				int yDirections = (i / 3) - 1;

				// the agent at that location.
				AreaComponent.AreaType at = getArea(area, x + xDirections, y + yDirections);
				if (at == null) {
					continue;
				}
				if (at.isStructure()) {
					continue;
				}

				Vector2i a = new Vector2i(x + xDirections, y + yDirections); // tile in vector form
				double gCost = current.gCost + getDistance(current.tile, a);
				double hCost = getDistance(a, goal);

				Node node = new Node(a, current, gCost, hCost);

				if (vecInlist(closedList, a) && gCost >= node.gCost) {
					continue;
				}

				if (!vecInlist(openList, a) || gCost < node.gCost) {
					openList.add(node);
				}
			}
		}

		closedList.clear();
		return null;
	}

	private boolean vecInlist(List<Node> list, Vector2i vector) {
		for (Node n : list) {
			if (n.tile.equals(vector)) {
				return true;
			}
		}
		return false;
	}

	private double getDistance(Vector2i tile, Vector2i goal) {
		double dx = Math.abs(tile.getX() - goal.getX());
		double dy = Math.abs(tile.getY() - goal.getY());
//		return Math.sqrt((dx * dx) + (dy * dy));
		return dx+dy;
	}

	private Comparator<Node> nodeSorter = new Comparator<Node>() {

		public int compare(Node n0, Node n1) {
			if (n1.fCost < n0.fCost) {
				return +1;
			}
			if (n1.fCost > n0.fCost) {
				return -1;
			}
			return 0;
		}
	};

	public AreaComponent.AreaType getArea(AreaComponent.AreaType[][] area, int x, int y) {

		boolean inbound = Utils.isInbound(x, y, 0, 0, area.length - 1, area[0].length - 1);
		if (inbound) {
			AreaComponent.AreaType type = area[x][y];
			if (type == OUTER_WALL || type == WALL) {
				return WALL;
			} else {
				return type;
			}

		} else {

			return WALL;
		}

	}

}
