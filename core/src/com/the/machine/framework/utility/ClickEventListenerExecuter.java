package com.the.machine.framework.utility;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 14/03/15
 */
public class ClickEventListenerExecuter extends ClickListener {
	private           Executable executable;

	public ClickEventListenerExecuter(Executable executable) {
		super();
		this.executable = executable;
	}

	@Override
	public void clicked(InputEvent event, float x, float y) {
		boolean handled = executable.execute();
		if (handled) {
			event.handle();
		}
	}

}
