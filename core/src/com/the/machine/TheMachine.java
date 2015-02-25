package com.the.machine;

import com.badlogic.gdx.ApplicationAdapter;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.engine.World;
import com.the.machine.scenes.SubEntityTestScene;

public class TheMachine extends ApplicationAdapter {

	World world;

	@Override
	public void create () {
		Asset.initialize();
		world = new World();
		world.create();
		world.buildScene(new SubEntityTestScene());
//		world.loadScene("Unnamed Scene");
	}

	@Override
	public void resize(int width, int height) {
		world.resize(width, height);
	}

	@Override
	public void pause() {
		world.pause();
	}

	@Override
	public void resume() {
		world.resume();
	}

	@Override
	public void render () {
		world.render();
	}

	@Override
	public void dispose() {
		Asset.dispose();
	}
}
