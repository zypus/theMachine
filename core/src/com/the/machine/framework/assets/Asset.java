package com.the.machine.framework.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 18/02/15
 */
public class Asset<T> {

	private String name;
	private Class<T> type;

	private transient T cachedAsset = null;

	private transient boolean ambiguous = false;

	private static Map<Class, Object> placeholders;
	private static Map<Class, Object> ambiguity;

	private static Texture progressTexture = null;

	private static SmartFileHandleResolver resolver;
	private static AssetManager            manager;

	public static void initialize() {
		resolver = new SmartFileHandleResolver();
		manager = new AssetManager(resolver);
		manager.setLoader(TextureRegion.class, new TextureRegionLoader(resolver));
		// load placeholders
		placeholders = new HashMap<>();
		Texture texture = new Texture(Gdx.files.local("missing/missing.jpg"));
		placeholders.put(Texture.class, texture);
		placeholders.put(TextureRegion.class, new TextureRegion(texture));
		placeholders.put(Skin.class, new Skin(Gdx.files.local("missing/missingskin.json")));
		// load ambiguity indicators
		ambiguity = new HashMap<>();
		Texture texture2 = new Texture(Gdx.files.local("missing/ambiguous.jpg"));
		ambiguity.put(Texture.class, texture2);
		ambiguity.put(TextureRegion.class, new TextureRegion(texture2));
		ambiguity.put(Skin.class, new Skin(Gdx.files.local("missing/ambiguousskin.json")));
		// progress texture
		progressTexture = new LoadingTexture(manager);
	}

	public static void dispose() {
		manager.dispose();
		// clear placeholder
		((Texture) placeholders.get(Texture.class)).dispose();
		((Skin) placeholders.get(Skin.class)).dispose();
		placeholders.clear();
		placeholders = null;
		// clear ambiguity
		((Texture) ambiguity.get(Texture.class)).dispose();
		((Skin) ambiguity.get(Skin.class)).dispose();
		ambiguity.clear();
		ambiguity = null;
		// clear progress texture
		progressTexture.dispose();
	}

	public static <A> Asset<A> fetch(String name, Class<A> type) {
		SmartFileHandleResolver.NAME_CHECK check = resolver.check(name);
		if (check == SmartFileHandleResolver.NAME_CHECK.EXISTS) {
			if (!manager.isLoaded(name, type)) {
				manager.load(name, type);
			}
		}
		Asset<A> asset = new Asset<>(name, type);
		if (check == SmartFileHandleResolver.NAME_CHECK.AMBIGUOUS) {
			asset.ambiguous = true;
		}
		return asset;
	}

	public T get() {
		if (cachedAsset == null) {
			if (manager.isLoaded(name, type)) {
				cachedAsset = manager.get(name, type);
			} else {
				float progress = manager.getProgress();
				if (ambiguous) {
					return (T) ambiguity.get(type);
				}
				if (progress < 1) {
					if (type.equals(Texture.class)) {
						return (T) progressTexture;
					} else if (type.equals(TextureRegion.class)) {
						return (T) new TextureRegion(progressTexture);
					} else {
						return (T) placeholders.get(type);
					}
				} else if (progressTexture != null){
					progressTexture.dispose();
					progressTexture = null;
				}
				return (T) placeholders.get(type);
			}
		}
		return cachedAsset;
	}

	public boolean isPlaceholder() {
		return cachedAsset == null;
	}

	private Asset(String name, Class<T> type) {
		this.name = name;
		this.type = type;
	}

	public static AssetManager getManager() {
		return manager;
	}

	private static class SmartFileHandleResolver
			implements FileHandleResolver {

		public static enum NAME_CHECK {
			EXISTS,
			MISSING,
			AMBIGUOUS
		}

		@Override
		public FileHandle resolve(String fileName) {
			FileHandle fileHandle = Gdx.files.local(fileName);
			if (fileHandle.exists()) {
				return fileHandle;
			}
			else {
				return findFile(Gdx.files.local(""), fileName);
			}
		}

		public NAME_CHECK check(String fileName) {
			FileHandle fileHandle = Gdx.files.local(fileName);
			if (fileHandle.exists()) {
				return NAME_CHECK.EXISTS;
			}
			List<FileHandle> files = findFiles(Gdx.files.local(""), fileName);
			if (files.size() == 0) {
				return NAME_CHECK.MISSING;
			} else if (files.size() == 1) {
				return NAME_CHECK.EXISTS;
			} else {
				return NAME_CHECK.AMBIGUOUS;
			}
		}

		private FileHandle findFile(FileHandle origin, String name) {
			if (origin.name().equals(name)
				|| origin.nameWithoutExtension().equals(name)
				|| origin.path().startsWith(name)) {
				return origin;
			} else {
				for (FileHandle child : origin.list()) {
					FileHandle result = findFile(child, name);
					if (result != null) {
						return result;
					}
				}
			}
			return null;
		}

		private List<FileHandle> findFiles(FileHandle origin, String name) {
			List<FileHandle> list = new ArrayList<>();
			if (origin.name()
					  .equals(name)
				|| origin.nameWithoutExtension()
						 .equals(name)
				|| origin.path()
						 .startsWith(name)) {
				list.add(origin);
				return list;
			} else {
				for (FileHandle child : origin.list()) {
					List<FileHandle> result = findFiles(child, name);
					list.addAll(result);
				}
			}
			return list;
		}
	}

	private Texture getProgressIndicatingTexture(float progress) {
		int size = 256;
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		FrameBuffer frameBuffer = new FrameBuffer(Pixmap.Format.RGB565, size, size, false);
		frameBuffer.begin();
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.arc(size / 2, size / 2, size / 2, 0, 360 * progress);
		shapeRenderer.flush();
		frameBuffer.end();

		return frameBuffer.getColorBufferTexture();
	}

}
