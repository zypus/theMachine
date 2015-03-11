package com.the.machine.framework.components.canvasElements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 18/02/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LabelComponent extends AbstractComponent {
	private String text = "";
	private Color color = Color.WHITE;
	private int alignment = Align.center;
}
