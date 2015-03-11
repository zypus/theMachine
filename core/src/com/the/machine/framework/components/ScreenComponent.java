package com.the.machine.framework.components;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Rectangle;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 11/02/15
 */
@Data
@AllArgsConstructor
public class ScreenComponent extends AbstractComponent {
	private Screen screen;
	private Rectangle dimension;
}
