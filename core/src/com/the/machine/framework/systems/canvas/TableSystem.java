package com.the.machine.framework.systems.canvas;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.DisabledComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.ButtonComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.components.canvasElements.TableComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;
import com.the.machine.framework.events.basic.ResizeEvent;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 18/02/15
 */
public class TableSystem extends IteratingSystem implements EventListener {

	private transient ComponentMapper<TableCellComponent>     tableCells     = ComponentMapper.getFor(TableCellComponent.class);
	private transient ComponentMapper<CanvasElementComponent> canvasElements = ComponentMapper.getFor(CanvasElementComponent.class);
	private transient ComponentMapper<ParentComponent>        parents        = ComponentMapper.getFor(ParentComponent.class);
	private transient ComponentMapper<TransformComponent>     transforms     = ComponentMapper.getFor(TransformComponent.class);
	private transient ComponentMapper<DimensionComponent>     dimensions     = ComponentMapper.getFor(DimensionComponent.class);
	private transient ComponentMapper<SubEntityComponent>     subs           = ComponentMapper.getFor(SubEntityComponent.class);
	private transient ComponentMapper<DisabledComponent>      disabled       = ComponentMapper.getFor(DisabledComponent.class);
	private transient ComponentMapper<TableComponent>      tables       = ComponentMapper.getFor(TableComponent.class);

	public TableSystem() {
		super(Family.all(SubEntityComponent.class, CanvasElementComponent.class)
					.one(TableComponent.class, ButtonComponent.class)
					.get());
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof ResizeEvent) {
			if (getEntities() != null) {
				for (Entity entity : getEntities()) {
					CanvasElementComponent elementComponent = canvasElements.get(entity);
					Table table = (Table) elementComponent.getUnwrappedActor();
					TableComponent tableComponent = tables.get(entity);
					if (table != null && tableComponent.isFillParent()) {
						table.invalidate();
					}
				}
			}
		}
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		CanvasElementComponent elementComponent = canvasElements.get(entity);
		// handle children
		if (subs.has(entity) && elementComponent.getActor() != null) {
			SubEntityComponent children = subs.get(entity);
			boolean invalidHierarchy = true;
			boolean cleared = false;
			Table table = (Table) elementComponent.getUnwrappedActor();
			int size = table.getChildren().size;
			while (invalidHierarchy) {
				for (Entity child : children) {
					if (canvasElements.has(child) && tableCells.has(child)) {
						CanvasElementComponent childElement = canvasElements.get(child);
						TableCellComponent cellComponent = tableCells.get(child);
						DimensionComponent dimensionComponent = dimensions.get(child);
						if (cleared || (!cellComponent.isAdded() && childElement.getActor() != null && !disabled.has(child))) {
							// it is not possible to insert cells, so if there is an actor which needs to be inserted we have to rebuild the table from scratch
							if (size != 0) {
								table.clearChildren();
								cleared = true;
								break;
							}
							Cell<Actor> cell = table.add(childElement.getActor());
							//							cell.width(dimensionComponent.getWidth());
							//							cell.height(dimensionComponent.getHeight());
							if (cellComponent.isRowEnd()) {
								cell.row();
							}
							cellComponent.setCell(cell);
							cellComponent.apply();
							cellComponent.setAdded(true);
							childElement.setAdded(true);
							cleared = false;
						} else if (childElement.isAdded() && disabled.has(child)) {
							// same as for inserting cells, it is not possible to remove cells so the table needs to be rebuild
							cellComponent.setCell(null);
							cellComponent.setAdded(false);
							childElement.setAdded(false);
							table.clearChildren();
							cleared = true;
							break;
						}
					}
				}
				if (!cleared) {
					invalidHierarchy = false;
				}
			}
		}
	}

}
