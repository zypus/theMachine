package com.the.machine.framework.components.physics;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.the.machine.framework.components.AbstractComponent;
import com.the.machine.framework.systems.rendering.LayerSortable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Light2dRenderComponent extends AbstractComponent implements LayerSortable {

	transient RayHandler rayHandler;
	boolean gammaCorrection;
	boolean useDiffuseLights;
	Color ambientLight;
	boolean blur;
	int blurNum;

	@Override
	public String getSortingLayer() {
		return "Lights 2d";
	}

	@Override
	public int getSortingOrder() {
		return 1;
	}
}
