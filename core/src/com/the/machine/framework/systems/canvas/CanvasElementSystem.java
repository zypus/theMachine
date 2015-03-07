package com.the.machine.framework.systems.canvas;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.CanvasComponent;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.DisabledComponent;
import com.the.machine.framework.components.ParentComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.ButtonComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.LabelComponent;
import com.the.machine.framework.components.canvasElements.SelectBoxComponent;
import com.the.machine.framework.components.canvasElements.SkinComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.components.canvasElements.TableComponent;
import com.the.machine.framework.components.canvasElements.TextFieldComponent;
import com.the.machine.framework.components.canvasElements.TreeComponent;
import com.the.machine.framework.components.canvasElements.TreeNodeComponent;
import com.the.machine.framework.utility.Enums;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/02/15
 */
public class CanvasElementSystem extends IteratingSystem implements EntityListener {

	transient private ComponentMapper<CanvasElementComponent> canvasElements = ComponentMapper.getFor(CanvasElementComponent.class);
	transient private ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	transient private ComponentMapper<DimensionComponent> dimensions = ComponentMapper.getFor(DimensionComponent.class);
	transient private ComponentMapper<ParentComponent> parents = ComponentMapper.getFor(ParentComponent.class);
	transient private ComponentMapper<SubEntityComponent> subs = ComponentMapper.getFor(SubEntityComponent.class);
	transient private ComponentMapper<DisabledComponent> disabled = ComponentMapper.getFor(DisabledComponent.class);
	transient private ComponentMapper<TableCellComponent> tableCells = ComponentMapper.getFor(TableCellComponent.class);
	transient private ComponentMapper<TreeNodeComponent> treeNodes = ComponentMapper.getFor(TreeNodeComponent.class);
	transient private ComponentMapper<SkinComponent>     skins    = ComponentMapper.getFor(SkinComponent.class);
	transient private ComponentMapper<CanvasComponent>     canvases    = ComponentMapper.getFor(CanvasComponent.class);

	transient private final Asset<Skin> defaultSkin = Asset.fetch("uiskin.json", Skin.class);

	// element component types
	transient private ComponentMapper<TableComponent>     tables      = ComponentMapper.getFor(TableComponent.class);
	transient private ComponentMapper<ButtonComponent>    buttons     = ComponentMapper.getFor(ButtonComponent.class);
	transient private ComponentMapper<LabelComponent>     labels      = ComponentMapper.getFor(LabelComponent.class);
	transient private ComponentMapper<TextFieldComponent> textFields  = ComponentMapper.getFor(TextFieldComponent.class);
	transient private ComponentMapper<SelectBoxComponent> selectBoxes = ComponentMapper.getFor(SelectBoxComponent.class);
	transient private ComponentMapper<TreeComponent>      trees       = ComponentMapper.getFor(TreeComponent.class);

	public CanvasElementSystem() {
		super(Family.all(CanvasElementComponent.class, TransformComponent.class, DimensionComponent.class)
					.one(CanvasComponent.class, TableComponent.class, ButtonComponent.class, LabelComponent.class, TextFieldComponent.class, SelectBoxComponent.class, TreeComponent.class)
					.get());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		engine.addEntityListener(Family.all(CanvasElementComponent.class, DimensionComponent.class).get(), this);
	}

	@Override
	public void removedFromEngine(Engine engine) {
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}

	@Override
	public void entityAdded(Entity entity) {
		updateCanvasElement(entity);
	}

	@Override
	public void entityRemoved(Entity entity) {
		CanvasElementComponent elementComponent = canvasElements.get(entity);
		elementComponent.getUnwrappedActor().remove();
		elementComponent.setAdded(false);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		updateCanvasElement(entity);
		CanvasElementComponent elementComponent = canvasElements.get(entity);
		// handle children
		if (parents.has(entity) && !canvases.has(entity) && parents.get(entity).getParent().get() == world.getWorldEntityRef().get()) {
			world.getSystem(CanvasSystem.class).getElementsToAdd().add(entity);
		}
		if (subs.has(entity) && elementComponent.getGroup() != null) {
			SubEntityComponent children = subs.get(entity);
			int index = 0;
			for (Entity child : children) {
				if (canvasElements.has(child) && !tableCells.has(child) && !treeNodes.has(child)) {
					CanvasElementComponent childElement = canvasElements.get(child);
					if (!childElement.isAdded() && childElement.getActor() != null && !disabled.has(child)) {
						elementComponent.getGroup()
										.addActorAt(index, childElement.getActor());
						childElement.setAdded(true);
					} else if (childElement.isAdded() && disabled.has(child)) {
						elementComponent.getGroup()
										.removeActor(childElement.getActor());
						childElement.setAdded(false);
					}
				}
			}
		}
	}

	protected void updateCanvasElement(Entity entity) {
		boolean hasMain = false;
		CanvasElementComponent elementComponent = canvasElements.get(entity);
		TransformComponent transform = transforms.get(entity);
		DimensionComponent dimension = dimensions.get(entity);
		if (tables.has(entity)) {
			resolveTypeChange(entity, Table.class);
			TableComponent tc = tables.get(entity);
			Table table;
			if (elementComponent.getActor() == null) {
				table = new Table();
				elementComponent.setActor(table);
				elementComponent.setGroup(true);
			} else {
				table = (Table) elementComponent.getActor();
			}
			table.setFillParent(tc.isFillParent());
			table.setDebug(tc.isDebug());
			if (tc.getHorizontalAlignment() == Enums.HorizontalAlignment.CENTER
				|| tc.getVerticalAlignment() == Enums.VerticalAlignment.CENTER) {
				table.center();
			}
			if (tc.getHorizontalAlignment() == Enums.HorizontalAlignment.LEFT) {
				table.left();
			} else if (tc.getHorizontalAlignment() == Enums.HorizontalAlignment.RIGHT) {
				table.right();
			}
			if (tc.getVerticalAlignment() == Enums.VerticalAlignment.TOP) {
				table.top();
			} else if (tc.getVerticalAlignment() == Enums.VerticalAlignment.BOTTOM) {
				table.bottom();
			}
			hasMain = true;
		} else if (buttons.has(entity)) {
			resolveTypeChange(entity, Button.class);
			ButtonComponent buttonComponent = buttons.get(entity);
			Button button;
			if (elementComponent.getActor() == null) {
				button = new Button(findSkin(entity).get());
				wrap(elementComponent, button);
				elementComponent.setGroup(true);
			} else {
				button = (Button) elementComponent.getUnwrappedActor();
			}
			button.setSkin(findSkin(entity).get());
			button.setStyle(findSkin(entity).get()
											.get("default", Button.ButtonStyle.class));
			button.setDisabled(!elementComponent.isEnabled());
			hasMain = true;
		} else if (labels.has(entity)) {
			resolveTypeChange(entity, Label.class);
			LabelComponent labelComponent = labels.get(entity);
			Label label;
			if (elementComponent.getActor() == null) {
				label = new Label(labelComponent.getText(), findSkin(entity).get());
				wrap(elementComponent, label);
				elementComponent.setGroup(false);
			} else {
				label = (Label) elementComponent.getUnwrappedActor();
			}
			label.setStyle(findSkin(entity).get()
										   .get("default", Label.LabelStyle.class));
			if (labelComponent.isDirty()) {
				label.setText(labelComponent.getText());
				label.setColor(labelComponent.getColor());
				label.setAlignment(labelComponent.getAlignment());
				labelComponent.setDirty(false);
			}
			hasMain = true;
		} else if (textFields.has(entity)) {
			resolveTypeChange(entity, TextField.class);
			TextFieldComponent textFieldComponent = textFields.get(entity);
			TextField textField;
			if (elementComponent.getActor() == null) {
				textField = new TextField(textFieldComponent.getText(), findSkin(entity).get());
				wrap(elementComponent, textField);
				elementComponent.setGroup(false);
				textFieldComponent.setTextField(textField);
			} else {
				textField = (TextField) elementComponent.getUnwrappedActor();
			}
			textField.setStyle(findSkin(entity).get()
										   .get("default", TextField.TextFieldStyle.class));
			if (textFieldComponent.isDirty()) {
				textField.setText(textFieldComponent.getText());
				textField.setMaxLength(textFieldComponent.getMaxLength());
				textField.setTextFieldListener(textFieldComponent.getTextFieldListeners());
				textField.setTextFieldFilter(textFieldComponent.getTextFieldFilter());
				textFieldComponent.setDirty(false);
			}
			hasMain = true;
		} else if (selectBoxes.has(entity)) {
			resolveTypeChange(entity, SelectBox.class);
			SelectBoxComponent selectBoxComponent = selectBoxes.get(entity);
			SelectBox selectBox;
			if (elementComponent.getActor() == null) {
				selectBox = new SelectBox(findSkin(entity).get());
				wrap(elementComponent, selectBox);
				elementComponent.setGroup(false);
			} else {
				selectBox = (SelectBox) elementComponent.getUnwrappedActor();
			}
			Skin skin = findSkin(entity).get();
			SelectBox.SelectBoxStyle style = skin
																.get("default", SelectBox.SelectBoxStyle.class);
			if (selectBox.getStyle() != style) {
				selectBox.setStyle(style);
				ScrollPane pane = selectBox.getScrollPane();
				pane.setStyle(skin.get("default", ScrollPane.ScrollPaneStyle.class));
				for (Actor actor : pane.getChildren()) {
					if (actor instanceof List) {
						((List) actor).setStyle(skin.get(List.ListStyle.class));
					}
				}
			}
			if (selectBoxComponent.isDirty()) {
				selectBox.setItems(selectBoxComponent.getItems());
				selectBoxComponent.setDirty(false);
			}
			hasMain = true;
		} else if (trees.has(entity)) {
			resolveTypeChange(entity, Tree.class);
			TreeComponent treeComponent = trees.get(entity);
			Tree tree;
			if (elementComponent.getActor() == null) {
				tree = new Tree(findSkin(entity).get());
				wrap(elementComponent, tree);
				elementComponent.setGroup(true);
			} else {
				tree = (Tree) elementComponent.getUnwrappedActor();
			}
			Skin skin = findSkin(entity).get();
			Tree.TreeStyle style = skin
					.get("default", Tree.TreeStyle.class);
			if (tree.getStyle() != style) {
				tree.setStyle(style);
			}
			hasMain = true;
		}

		// if there is no main component make own actor a simple group component
		if (!hasMain && elementComponent.getActor() == null) {
			elementComponent.setActor(new Group());
			elementComponent.setGroup(true);
		}

		Actor actor = elementComponent.getUnwrappedActor();
		if (actor.getUserObject() != null) {
			actor.setUserObject(new WeakReference<>(entity));
		}

		if (!tableCells.has(entity) && !treeNodes.has(entity) && !(tables.has(entity) && tables.get(entity).isFillParent())) {



			actor.setZIndex((int) transform.getZ());
			//FIXME Fix stuff related to transform = true for tables, etc.
			DimensionComponent dim = dimension;
			if (elementComponent.isWrapped()) {
				Actor unwrappedActor = elementComponent.getUnwrappedActor();
				unwrappedActor.setBounds(transform.getX() - dim.getWidth() * dim.getOriginX(), transform.getY() - dim.getHeight() * dim.getOriginY(), dim.getWidth(), dim.getWidth());
				unwrappedActor.setOrigin(dim.getWidth() * dim.getOriginX(), dim.getHeight() * dim.getOriginY());
				actor.setOrigin(transform.getX() + dim.getWidth() * (dim.getPivotX() - dim.getOriginX()), transform.getY() + dim.getHeight() * (dim.getPivotY() - dim.getOriginY()));
				actor.setBounds(transform.getX(), transform.getY(), dim.getWidth(), dim.getWidth());
			} else {
//				actor.setBounds(transform.getX(), transform.getY(), dimension.getWidth(), dimension.getWidth());
//				actor.setOrigin(dimension.getOriginX() * actor.getWidth(), dimension.getOriginY() * actor.getHeight());
				actor.setBounds(transform.getX() - dim.getWidth() * dim.getOriginX(), transform.getY() - dim.getHeight() * dim.getOriginY(), dim.getWidth(), dim.getWidth());
				actor.setOrigin(dim.getWidth() * dim.getOriginX(), dim.getHeight() * dim.getOriginY());
			}
			actor.setScale(transform.getXScale(), transform.getYScale());
			actor.setRotation(transform.getZRotation());
//			actor.setRotation(actor.getRotation()+1); // just continuously rotating the actor for testing
			actor.setVisible(elementComponent.isEnabled());
		}

	}

	private Asset<Skin> findSkin(Entity entity) {
		Asset<Skin> skin = defaultSkin;
		if (skins.has(entity)) {
			skin = skins.get(entity)
						.getSkin();
		} else if (parents.has(entity)) {
			SkinComponent component = parents.get(entity)
											 .askForNext(SkinComponent.class, skins, parents);
			if (component != null) {
				skin = component.getSkin();
			}
		}
		return skin;
	}

	private void resolveTypeChange(Entity entity, Class<? extends Actor> type) {
		CanvasElementComponent elementComponent = canvasElements.get(entity);
		if (elementComponent.getUnwrappedActor() != null && type != elementComponent.getUnwrappedActor().getClass()) {
			if (elementComponent.isAdded() && parents.has(entity)) {
				Entity parent = parents.get(entity)
									   .getParent().get();
				if (parent != null) {
					canvasElements.get(parent)
								  .getGroup()
								  .removeActor(elementComponent.getUnwrappedActor());
				}
			}
			elementComponent.setActor(null);
			elementComponent.setAdded(false);
		}
	}

	private <T extends Actor> void wrap(CanvasElementComponent elementComponent, T actor) {
		if (elementComponent.isEnableTransform()) {
			Container<T> container = new Container<>(actor);
			container.setTransform(true);
			elementComponent.setActor(container);
			elementComponent.setUnwrappedActor(actor);
		} else {
			elementComponent.setActor(actor);
		}
	}
}
