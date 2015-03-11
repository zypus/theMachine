package com.the.machine.framework.systems.canvas;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.utility.Enums;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 19/02/15
 */
public class TableCellSystem extends IteratingSystem {

	private transient ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	private transient ComponentMapper<DimensionComponent> dimensions = ComponentMapper.getFor(DimensionComponent.class);
	private transient ComponentMapper<TableCellComponent> tableCells = ComponentMapper.getFor(TableCellComponent.class);
	private transient ComponentMapper<CanvasElementComponent> canvasElements = ComponentMapper.getFor(CanvasElementComponent.class);
	private transient ComponentMapper<ParentComponent> parents = ComponentMapper.getFor(ParentComponent.class);

	public TableCellSystem() {
		super(Family.all(TableCellComponent.class, CanvasElementComponent.class, TransformComponent.class, DimensionComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		CanvasElementComponent elementComponent = canvasElements.get(entity);
		Actor actor = elementComponent.getActor();
		if (actor != null) {
			TransformComponent transform = transforms.get(entity);
			DimensionComponent dimension = dimensions.get(entity);
			Vector3 actorPosition = new Vector3(actor.getX(), actor.getY(), actor.getZIndex());
			if (!transform.getPosition()
						  .equals(actorPosition)) {
				transform.setPosition(actor.getX(), actor.getY(), actor.getZIndex());
				transform.notifyObservers("Position");
			}
			if (dimension.getWidth() != actor.getWidth() || dimension.getHeight() != actor.getHeight()) {
				dimension.setWidth(actor.getWidth());
				dimension.setHeight(actor.getHeight());
				dimension.notifyObservers("Dimension");
			}
		}
		TableCellComponent cellComponent = tableCells.get(entity);
		TableCellComponent cc = cellComponent;
		Cell cell = cc.getCell();
		if (cell != null && cc.isDirty()) {
			cell.minSize(cc.getMinWidth(), cc.getMinHeight())
				.prefSize(cc.getPrefWidth(), cc.getPrefHeight())
				.maxSize(cc.getMaxWidth(), cc.getMinHeight());
			cell.expand(cc.getExpandX(), cc.getExpandY())
				.fill(cc.getFillX(), cc.getExpandY());
			cell.space(cc.getSpaceTop(), cc.getSpaceLeft(), cc.getSpaceBottom(), cc.getSpaceRight());
			cell.pad(cc.getPadTop(), cc.getPadLeft(), cc.getPadBottom(), cc.getPadRight());
			cell.uniform(cc.isUniformX(), cc.isUniformY());
			cell.colspan(cc.getColspan());
			if (cc.getHorizontalAlignment() == Enums.HorizontalAlignment.CENTER || cc.getVerticalAlignment() == Enums.VerticalAlignment.CENTER) {
				cell.center();
			}
			if (cc.getHorizontalAlignment() == Enums.HorizontalAlignment.LEFT) {
				cell.left();
			} else if (cc.getHorizontalAlignment() == Enums.HorizontalAlignment.RIGHT) {
				cell.right();
			}
			if (cc.getVerticalAlignment() == Enums.VerticalAlignment.TOP) {
				cell.top();
			} else if (cc.getVerticalAlignment() == Enums.VerticalAlignment.BOTTOM) {
				cell.bottom();
			}
			cellComponent.setDirty(false);
		}
	}
}
