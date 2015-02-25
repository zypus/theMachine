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
public class IntegerInterfaceBuilder implements InterfaceBuilder {
	@Override
	public Entity interfaceFor(Interfacer interfacer, Object object, Class<?> aClass, Object owner, Field field) {
		Integer integer = (Integer) object;
		IntegerHandler handler = new IntegerHandler(integer);
		TextFieldComponent textFieldComponent = new TextFieldComponent().setText(integer.toString()).setTextFieldListeners(handler).setTextFieldFilter(new IntegerFilter());
		handler.setTextFieldComponent(textFieldComponent);
		Entity intEntity = EntityUtilities.makeTextField(textFieldComponent);
		if (owner instanceof Observable) {
			((Observable) owner).addObserver(handler);
			handler.setObservable((Observable) owner);
		}
		return intEntity;
	}

	private static class IntegerHandler implements Observer, TextField.TextFieldListener {

		static Class<Integer> integerClass = Integer.class;
		private boolean changed = false;
		@Setter private Observable observable;
		@Setter TextFieldComponent textFieldComponent;
		Integer integer;

		private IntegerHandler(Integer integer) {
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
					changed = true;
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
				textFieldComponent.setText("" + integer.intValue());
				textFieldComponent.notifyObservers();
			}
			changed = false;
		}
	}

}
