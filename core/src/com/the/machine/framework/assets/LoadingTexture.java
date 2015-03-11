package com.the.machine.framework.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 19/02/15
 */
public class LoadingTexture extends Texture {

	private static final int size = 128;
	private static AssetManager loadingManager;
	private static FrameBuffer frameBuffer = null;
	private static OrthographicCamera camera = new OrthographicCamera();
	private static ShapeRenderer shapeRenderer = new ShapeRenderer();
	private static float   lastProgress   = -1;

	public LoadingTexture(AssetManager manager) {
		super(size, size, Pixmap.Format.RGB565);
		loadingManager = manager;
		computeProgressIndicatingTexture(0);
	}

	public static void recompute() {
		if (loadingManager != null) {
			float progress = loadingManager.getProgress();
			if (progress != lastProgress) {
				lastProgress = progress;
				computeProgressIndicatingTexture(progress);
			}
		}
	}

	private static void computeProgressIndicatingTexture(float progress) {
		if (frameBuffer == null) {
			frameBuffer = new FrameBuffer(Pixmap.Format.RGB565, size, size, false);
			camera.setToOrtho(true, size, size);
		}
		frameBuffer.begin();
		Gdx.gl20.glClearColor(0, 0, 0, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setColor(Color.WHITE);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.arc(size / 2, size / 2, size / 2, 0, 360 * progress);
		shapeRenderer.end();
		frameBuffer.end();
	}

	@Override
	public void bind() {
		if (frameBuffer != null) {
			frameBuffer.getColorBufferTexture().bind();
		} else {
			super.bind();
		}
	}
}
