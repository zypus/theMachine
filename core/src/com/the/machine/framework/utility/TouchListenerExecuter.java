package com.the.machine.framework.utility;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 14/03/15
 */
public class TouchListenerExecuter extends InputListener {

	private Executable downExecutable;
	private Executable dragExecutable;
	private Executable upExecutable;

	public TouchListenerExecuter(Executable downExecutable, Executable dragExecutable, Executable upExecutable) {
		this.downExecutable = downExecutable;
		this.dragExecutable = dragExecutable;
		this.upExecutable = upExecutable;
	}

	@Override
	public boolean handle(Event e) {
		return super.handle(e);
	}

	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
		if (downExecutable != null) {
			return downExecutable.execute();
		}
		return false;
	}

	@Override
	public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
		if (upExecutable != null) {
			if (upExecutable.execute()) {
				event.handle();
			}
		}
	}

	@Override
	public void touchDragged(InputEvent event, float x, float y, int pointer) {
		if (dragExecutable != null) {
			if (dragExecutable.execute()) {
				event.handle();
			}
		}
	}
}
