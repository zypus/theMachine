package com.the.machine.framework.utility.interfaceBuilder;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/02/15
 */
public class IntegerFilter
		implements TextField.TextFieldFilter {

	@Override
	public boolean acceptChar(TextField textField, char c) {
		if (Character.isDigit(c)) {
			return true;
		} else if (c == '-' && !textField.getText()
										 .matches("-{1}.*") && textField.getCursorPosition() == 0) {
			return true;
		}
		return false;
	}
}
