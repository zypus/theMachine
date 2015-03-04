package com.the.machine.framework.utility.interfaceBuilder;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.the.machine.framework.components.canvasElements.TextFieldComponent;
import com.the.machine.framework.interfaces.Observable;
import com.the.machine.framework.interfaces.Observer;
import com.the.machine.framework.utility.EntityUtilities;
import com.the.machine.framework.utility.InterfaceBuilder;
import com.the.machine.framework.utility.Interfacer;
import lombok.Setter;

import java.lang.reflect.Field;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/02/15
 */
public class IntegerInterfaceBuilder implements InterfaceBuilder<Integer> {
	@Override
	public Entity interfaceFor(Interfacer interfacer, Integer integer, Class<?> aClass, Object owner, Field field) {
		Entity intEntity;
		IntegerHandler handler = new IntegerHandler(owner, field);
		TextFieldComponent textFieldComponent = new TextFieldComponent().setText(integer.toString())
																		.setTextFieldListeners(handler)
																		.setTextFieldFilter(new IntegerFilter());
		handler.setTextFieldComponent(textFieldComponent);

  		intEntity = EntityUtilities.makeTextField(textFieldComponent);

		return intEntity;
	}

	private static class IntegerHandler implements Observer, TextField.TextFieldListener {

		private int last = 0;
		private boolean changed = false;
		private Observable observable = null;
		private Object owner;
		private Field field;
		@Setter TextFieldComponent textFieldComponent;

		public IntegerHandler(Object owner, Field field) {
			this.owner = owner;
			this.field = field;
			field.setAccessible(true);
			if (owner instanceof Observable) {
				observable = (Observable) owner;
				observable.addObserver(this);
			}
		}

		@Override
		public void keyTyped(TextField textField, char c) {
			if (c == '\r') {
				observable.forceChanged();
				observable.notifyObservers();
				return;
			}
			int v;
			if (!Character.isDigit(c) && !Character.isAlphabetic(c) && c != '-' && !Character.isWhitespace(c)) {
				return;
			}
			if (textField.getText()
						 .equals("") || textField.getText()
												 .equals("-")) {
				v = 0;
			} else {
				v = Integer.parseInt(textField.getText());
			}
			if (last == v || ( last > -10 && last < 10 && v > -10 && v < 10 ) ) {
				textFieldComponent.setText("" + v);
			}
			if (last != v) {
				try {
					field.setInt(owner, v);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				if (observable != null) {
					changed = true;
					observable.forceChanged();
					observable.notifyObservers();
				}
			}
			last = v;
		}

		@Override
		public void update(Observable o, Object arg) {
			if (!changed) {
				try {
					textFieldComponent.setText("" + field.getInt(o));
					textFieldComponent.notifyObservers();
				} catch (IllegalAccessException e) {
//						e.printStackTrace();
				}
			}
			changed = false;
		}
	}

	private static class SoloIntegerHandler
			implements Observer, TextField.TextFieldListener {

		static  Class<Integer> integerClass = Integer.class;
		private boolean        changed      = false;
		@Setter private Observable         observable;
		@Setter         TextFieldComponent textField;
		Integer integer;

		private SoloIntegerHandler(Integer integer) {
			this.integer = integer;
		}

		@Override
		public void keyTyped(TextField textField, char c) {
			int v;
			if (textField.getText()
						 .equals("") || textField.getText()
												 .equals("-")) {
				v = 0;
			} else {
				v = Integer.parseInt(textField.getText());
			}
			try {
				Field value = integerClass.getDeclaredField("value");
				value.setAccessible(true);
				value.set(integer, v);
				if (observable != null) {
//					changed = true;
					observable.forceChanged();
					observable.notifyObservers();
				}
			} catch (NoSuchFieldException e) {
				//				e.printStackTrace();
			} catch (IllegalAccessException e) {
				//				e.printStackTrace();
			}
		}

		@Override
		public void update(Observable o, Object arg) {
			if (!changed) {
				int position = textField.getTextField()
										.getCursorPosition();
				textField.setText("" + integer.intValue());
				textField.notifyObservers();
				textField.getTextField().setCursorPosition(position);
			}
			changed = false;
		}
	}

}
