package com.the.machine.framework.components.canvasElements;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.utils.Array;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Getter;
import lombok.Setter;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/02/15
 */
@Getter
public class SelectBoxComponent extends AbstractComponent {
	@Setter transient SelectBox selectBox;
	Array items = new Array();

	public SelectBoxComponent setItems(Array items) {
		this.items = items;
		dirty = true;
		setChanged();
		return this;
	}
}
