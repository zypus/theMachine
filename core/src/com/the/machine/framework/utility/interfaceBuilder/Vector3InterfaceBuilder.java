package com.the.machine.framework.utility.interfaceBuilder;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.the.machine.framework.components.DimensionComponent;
import com.the.machine.framework.components.SubEntityComponent;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.CanvasElementComponent;
import com.the.machine.framework.components.canvasElements.TableCellComponent;
import com.the.machine.framework.components.canvasElements.TableComponent;
import com.the.machine.framework.components.canvasElements.TextFieldComponent;
import com.the.machine.framework.interfaces.Observable;
import com.the.machine.framework.interfaces.Observer;
import com.the.machine.framework.utility.EntityUtilities;
import com.the.machine.framework.utility.Enums;
import com.the.machine.framework.utility.InterfaceBuilder;
import com.the.machine.framework.utility.Interfacer;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/02/15
 */
public class Vector3InterfaceBuilder
		implements InterfaceBuilder<Vector3> {
	@Override
	public Entity interfaceFor(Interfacer interfacer, Vector3 vector3, Class<?> aClass, Object owner, Field field) {
		Entity table = new Entity();
		table.add(new TableComponent().setHorizontalAlignment(Enums.HorizontalAlignment.LEFT));
		table.add(new CanvasElementComponent());
		table.add(new TransformComponent());
		table.add(new DimensionComponent());
		SubEntityComponent entities = new SubEntityComponent();
		table.add(entities);

		Vector3Handler handler = new Vector3Handler(vector3);

		entities.add(EntityUtilities.relate(table, EntityUtilities.makeLabel("x"))
								  .add(new TableCellComponent().setSpaceRight(new Value.Fixed(10))));
		TextFieldComponent x = new TextFieldComponent().setText("" + vector3.x)
													   .setTextFieldListeners(new Vector3Listener(handler, "x"))
													   .setTextFieldFilter(new FloatFilter());
		entities.add(EntityUtilities.relate(table, EntityUtilities.makeTextField(x))
								  .add(new TableCellComponent().setSpaceRight(new Value.Fixed(10))
															   .setWidth(new Value.Fixed(60))));
		entities.add(EntityUtilities.relate(table, EntityUtilities.makeLabel("y"))
								  .add(new TableCellComponent().setSpaceRight(new Value.Fixed(10))));
		TextFieldComponent y = new TextFieldComponent().setText("" + vector3.y)
													   .setTextFieldListeners(new Vector3Listener(handler, "y"))
													   .setTextFieldFilter(new FloatFilter());
		entities.add(EntityUtilities.relate(table, EntityUtilities.makeTextField(y))
								  .add(new TableCellComponent().setSpaceRight(new Value.Fixed(10))
															   .setWidth(new Value.Fixed(60))));
		entities.add(EntityUtilities.relate(table, EntityUtilities.makeLabel("z"))
								  .add(new TableCellComponent().setSpaceRight(new Value.Fixed(10))));
		TextFieldComponent z = new TextFieldComponent().setText("" + vector3.z)
													   .setTextFieldListeners(new Vector3Listener(handler, "z"))
													   .setTextFieldFilter(new FloatFilter());
		entities.add(EntityUtilities.relate(table, EntityUtilities.makeTextField(z))
								  .add(new TableCellComponent().setSpaceRight(new Value.Fixed(10))
															   .setWidth(new Value.Fixed(60))));

		handler.registerTextfield(x, "x");
		handler.registerTextfield(y, "y");
		handler.registerTextfield(z, "z");

		if (owner instanceof Observable) {
			((Observable) owner).addObserver(handler);
			handler.setObservable((Observable) owner);
		}

		return table;
	}

	private static class Vector3Listener extends FocusListener
			implements TextField.TextFieldListener {

		Vector3Handler handler;
		String  field;
		private boolean added = false;

		private Vector3Listener(Vector3Handler handler, String field) {
			this.handler = handler;
			this.field = field;
		}

		@Override
		public void keyTyped(TextField textField, char c) {
			if (!added) {
				textField.addListener(this);
				added = true;
			}
			float v;
			if (textField.getText()
						 .equals("") || textField.getText()
												 .equals("-")) {
				v = 0;
			} else {
				v = Float.parseFloat(textField.getText());
			}
			handler.set(v, field);
		}

		@Override
		public boolean handle(Event event) {
			if (event.toString()
					 .equals("mouseMoved")
				|| event.toString()
						.equals("exit")
				|| event.toString()
						.equals("enter")
				|| event.toString()
						.equals("keyDown")
				|| event.toString()
						.equals("touchUp")) {

				handler.setActive(false, field);
				return false;
			}
			handler.setActive(true, field);

			return true;
		}

	}

	private class Vector3Handler implements Observer {

		private boolean changed = false;
		@Setter private Observable observable;
		Vector3            vector3;
		TextFieldComponent xField;
		TextFieldComponent yField;
		TextFieldComponent zField;
		private boolean activeX = false;
		private boolean activeY = false;
		private boolean activeZ = false;

		private Vector3Handler(Vector3 vector3) {
			this.vector3 = vector3;
		}

		public void registerTextfield(TextFieldComponent textField, String field) {
			switch (field) {
				case "x":
					xField = textField;
					break;
				case "y":
					yField = textField;
					break;
				case "z":
					zField = textField;
					break;
			}
		}

		public void setActive(boolean active, String field) {
			switch (field) {
				case "x":
					activeX = active;
					break;
				case "y":
					activeY = active;
					break;
				case "z":
					activeZ = active;
					break;
			}
		}


		public synchronized void set(float v, String field) {
			switch (field) {
				case "x":
					if (vector3.x == v) {
						return;
					}
					vector3.x = v;
					break;
				case "y":
					if (vector3.y == v) {
						return;
					}
					vector3.y = v;
					break;
				case "z":
					if (vector3.z == v) {
						return;
					}
					vector3.z = v;
					break;
			}
			if (observable != null) {
				observable.forceChanged();
				observable.notifyObservers();
			}
		}

		@Override
		public void update(Observable o, Object arg) {
			if (!changed) {
				if (!activeX) {
					xField.setText("" + vector3.x);
					xField.notifyObservers();
				}
				if (!activeY) {
					yField.setText("" + vector3.y);
					yField.notifyObservers();
				}
				if (!activeZ) {
					zField.setText("" + vector3.z);
					zField.notifyObservers();
				}
			}
			changed = false;
		}
	}

	private static class MaxLengthWidthValue
			extends Value {

		@Getter private static MaxLengthWidthValue instance = new MaxLengthWidthValue();

		private MaxLengthWidthValue() {
		}

		@Override
		public float get(Actor context) {
			TextField textField = (TextField) context;
			int maxLength = textField.getMaxLength();
			if (maxLength <= 0) {
				return textField.getPrefWidth();
			} else {
				return maxLength * textField.getStyle().font.getSpaceWidth();
			}
		}
	}
}
