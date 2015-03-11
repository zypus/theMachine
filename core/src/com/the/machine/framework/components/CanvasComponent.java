package com.the.machine.framework.components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.the.machine.framework.systems.rendering.LayerSortable;
import lombok.Data;

/**
 * CanvasComponents are components on top of the rendered objects. Can for example also have the ButtonComponent
 * or a LabelComponent
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 16/02/15
 */
@Data
public class CanvasComponent extends AbstractComponent implements LayerSortable {
	transient private SpriteBatch batch;
	transient private Stage stage;
	private boolean debug = false;

	@Override
	public String getSortingLayer() {
		return "UI";
	}

	@Override
	public int getSortingOrder() {
		return 0;
	}
}
