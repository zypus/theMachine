package com.the.machine.framework.components.canvasElements;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/02/15
 */
@Getter
@Accessors(chain = true)
public class ButtonComponent extends AbstractComponent {
	@Setter transient Button button;
	ButtonGroup<Button> buttonGroup = null;

	public ButtonComponent setButtonGroup(ButtonGroup<Button> buttonGroup) {
		if (this.buttonGroup != buttonGroup) {
			setChanged();
			dirty = true;
		}
		this.buttonGroup = buttonGroup;
		return this;
	}
}
