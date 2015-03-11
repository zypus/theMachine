package com.the.machine.framework.components.physics;

import com.badlogic.gdx.physics.box2d.World;
import com.the.machine.framework.components.AbstractComponent;
import com.the.machine.framework.systems.rendering.LayerSortable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 10/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Physics2dDebugComponent extends AbstractComponent implements LayerSortable {
	transient World box2dWorld;
	float boxToWorld;

	@Override
	public String getSortingLayer() {
		return "Physics 2d Debug";
	}

	@Override
	public int getSortingOrder() {
		return 0;
	}
}
