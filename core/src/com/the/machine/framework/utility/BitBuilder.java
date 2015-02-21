package com.the.machine.framework.utility;

import com.badlogic.gdx.utils.Bits;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/02/15
 */
public class BitBuilder {

	int size;
	Bits bits;

	public static BitBuilder all(int size) {
		return new BitBuilder(size).setAll();
	}

	public static BitBuilder none(int size) {
		return new BitBuilder(size);
	}

	public static String sprint(Bits b) {
		if (b == null) {
			return "null";
		}
		if (b.length() == 0) {
			return "none";
		}
		String bitString = "";
		for (int i = 0; i < b.length(); i++) {
			bitString += (b.get(i))
						 ? "1"
						 : "0";
		}
		return bitString;
	}

	private BitBuilder(int size) {
		this.size = size;
		bits = new Bits(size);
	}

	public BitBuilder setAll() {
		for (int i = 0; i < size; i++) {
			bits.set(i);
		}
		return this;
	}

	public BitBuilder clearAll() {
		bits.clear();
		return this;
	}

	public BitBuilder s(int i) {
		bits.set(i);
		return this;
	}

	public BitBuilder c(int i) {
		bits.clear(i);
		return this;
	}

	public BitBuilder setRange(int f, int t) {
		for (int i = f; i <= t; i++) {
			bits.set(i);
		}
		return this;
	}

	public BitBuilder clearRange(int f, int t) {
		for (int i = f; i <= t; i++) {
			bits.clear(i);
		}
		return this;
	}

	public BitBuilder sb(int b) {
		for (int i = 0; i <= size; i++) {
			if (((int)Math.pow(2, i) & b) == 1) {
				bits.set(i);
			}
		}
		return this;
	}

	public BitBuilder cb(int b) {
		for (int i = 0; i <= size; i++) {
			if (((int) Math.pow(2, i) & b) == 1) {
				bits.clear(i);
			}
		}
		return this;
	}

	public Bits get() {
		return bits;
	}

}
