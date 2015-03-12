package com.the.machine.framework.components.physics;

import com.the.machine.framework.components.AbstractComponent;
import com.the.machine.framework.systems.rendering.LayerSortable;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/03/15
 */
public class Light2dRenderComponent extends AbstractComponent implements LayerSortable {

	@Override
	public String getSortingLayer() {
		return "Lights 2d";
	}

	@Override
	public int getSortingOrder() {
		return 1;
	}
}
