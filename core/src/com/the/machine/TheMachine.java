package com.the.machine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.the.machine.events.MapEditorLoadEvent;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.engine.World;
import com.the.machine.scenes.MapEditorSceneBuilder;
import lombok.Setter;

import javax.swing.JOptionPane;
import java.util.List;

public class TheMachine extends ApplicationAdapter {

	World world;

	@Setter boolean testMode = true;

	@Override
	public void create () {
		int i = JOptionPane.showOptionDialog(null, "Test mode?", "Mode Selection", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		testMode = (i == 0);
		Asset.initialize();
		world = new World();
		world.setTimeFlow(15);
		world.create();
		world.buildScene(new MapEditorSceneBuilder());
		Gdx.input.setInputProcessor(world.getInputMultiplexer());
		if (testMode) {
			List<FileHandle> prefabs = new Asset.SmartFileHandleResolver().findFiles(Gdx.files.local("prefabs"), ".map.prefab");
			Array<String> names = new Array<>(prefabs.size());
			for (FileHandle prefab : prefabs) {
				names.add(prefab.nameWithoutExtension()
								.split("\\.")[0]);
			}
			String[] strings = new String[names.size];
			int index=0;
			for (String name : names) {
				strings[index] = name;
				index++;
			}
			String s = (String) JOptionPane.showInputDialog(null, "Select map:", "Map Selection", JOptionPane.INFORMATION_MESSAGE, null, strings, strings[0]);
			world.dispatchEvent(new MapEditorLoadEvent(s));
		}
//		world.updateActiveScene();
//		world.saveActiveScene();
//		world.loadScene("Unnamed Scene");
	}

	@Override
	public void resize(int width, int height) {
		world.resize(width, height);
	}

	@Override
	public void pause() {
//		world.pause();
	}

	@Override
	public void resume() {

//		world.resume();
	}

	long pastTime = 0;
	long lastTime = System.currentTimeMillis();
	int frames = 0;
	boolean loaded = false;

	@Override
	public void render () {
		if (testMode) {
			world.setFixedDelta(0.1f);
			while (true) {
				world.render();
				if (!loaded) {
					MapEditorSceneBuilder.toggleSimulationSystems(world, true);
					loaded = true;
				}
				long newTime = System.currentTimeMillis();
				pastTime += newTime - lastTime;
				lastTime = newTime;
				frames++;
				if (pastTime >= 1000) {
					pastTime = pastTime % 1000;
					System.out.println("FPS: " + frames);
					frames = 0;
				}
			}
		} else {
			world.render();
		}

	}

	@Override
	public void dispose() {
		Asset.dispose();
	}
}
