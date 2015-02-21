package com.the.machine.framework.components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.the.machine.framework.systems.rendering.LayerSortable;
import lombok.Data;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 16/02/15
 */
@Data
public class CanvasComponent extends AbstractComponent implements LayerSortable {
	private SpriteBatch batch;
	private Stage stage;
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
