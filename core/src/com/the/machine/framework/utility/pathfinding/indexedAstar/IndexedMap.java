package com.the.machine.framework.utility.pathfinding.indexedAstar;

import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

public class IndexedMap<K, V extends IndexableNode<V>>
		extends HashMap<K, V> {

	Array<V> a = new Array<>();

	@Override
	public V put(K key, V value) {
		V v = get(key);
		if (v != null) {
			int index = v.getIndex();
			if (value != null) {
				value.setIndex(index);
			} else {
				a.set(index, null);
			}
		} else if (value != null) {
			int index = a.size;
			value.setIndex(index);
			a.add(value);
		}
		return super.put(key, value);
	}

	public V get(int index) {
		if (a.size > 0 && index >= 0 && index < a.size) {
			return a.get(index);
		} else {
			return null;
		}
	}
}
