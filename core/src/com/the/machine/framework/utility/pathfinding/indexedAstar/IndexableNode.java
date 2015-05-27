package com.the.machine.framework.utility.pathfinding.indexedAstar;

import com.badlogic.gdx.ai.pfa.indexed.IndexedNode;
import lombok.Data;

@Data
public abstract class IndexableNode<N extends IndexedNode<N>>
		implements IndexedNode<N> {

	private int index = 0;

}
