package com.the.machine.framework.utility;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 04/03/15
 */
public class Utils {

	public static boolean isInbound(float x, float y, float bx, float by, float w, float h) {
		return x >= bx && x <= bx+w && y >= by && y <= by+h;
	}

	public static boolean isInboundX(float x, float bx, float w) {
		return x >= bx && x <= bx+w;
	}

	public static boolean isInboundY(float y, float by, float h) {
		return y >= by && y <= by+h;
	}

}
