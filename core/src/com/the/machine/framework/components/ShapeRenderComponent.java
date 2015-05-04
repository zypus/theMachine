package com.the.machine.framework.components;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.the.machine.framework.systems.rendering.LayerSortable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ShapeRenderComponent extends AbstractComponent implements LayerSortable {

	private String sortingLayer = "Default";
	private int   sortingOrder = 0;
	private List<Shape> shapes = new ArrayList<>();

	public ShapeRenderComponent add(Shape shape) {
		shapes.add(shape);
		return this;
	}

	public ShapeRenderComponent remove(Shape shape) {
		shapes.remove(shape);
		return this;
	}

	public static interface Shape {
		void render(ShapeRenderer renderer);
	}

}
