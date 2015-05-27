package com.the.machine.framework.utility.pathfinding.indexedAstar;

import com.badlogic.gdx.ai.pfa.Connection;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TiledConnection
		implements Connection<TiledNode> {

	TiledNode fNode;
	TiledNode tNode;
	float     c;

	@Override
	public float getCost() {
		return c;
	}

	@Override
	public TiledNode getFromNode() {
		return fNode;
	}

	@Override
	public TiledNode getToNode() {
		return tNode;
	}
}
