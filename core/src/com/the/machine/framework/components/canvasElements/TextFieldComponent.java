package com.the.machine.framework.components.canvasElements;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/02/15
 */
@Getter
@Accessors(chain = true)
public class TextFieldComponent extends AbstractComponent {
	@Setter private transient TextField                   textField          = null;
	private                   String                      text               = "";
	private                   int                         maxLength          = 0;
	private                   TextField.TextFieldFilter   textFieldFilter    = null;
	private                   TextField.TextFieldListener textFieldListeners = null;

	public TextFieldComponent setText(String text) {
		this.text = text;
		dirty = true;
		setChanged();
		return this;
	}

	public TextFieldComponent setMaxLength(int maxLength) {
		this.maxLength = maxLength;
		dirty = true;
		setChanged();
		return this;
	}

	public TextFieldComponent setTextFieldFilter(TextField.TextFieldFilter textFieldFilter) {
		this.textFieldFilter = textFieldFilter;
		return this;
	}

	public TextFieldComponent setTextFieldListeners(TextField.TextFieldListener textFieldListeners) {
		this.textFieldListeners = textFieldListeners;
		return this;
	}
}
