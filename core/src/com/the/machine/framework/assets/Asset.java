package com.the.machine.framework.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulation for the asset management. Regarding resources this is the only interaction point. The class provides access to the
 * requested asset. However before the actual asset is loaded a replacement resource is used, which will be discarded once the
 * real resource is available. One down side at the moment is that the resource need to be get() once agin, when the final resource is
 * available.
 *
 * Moreover provides a way to serialize the asset without storing the actual resource again. So this asset class act only as a reference
 * in this way. However it is not called AssetReference, simply because I find it confusing to think about references, when I simply want
 * to have my images drawn.
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 18/02/15
 */
public class Asset<T> {

	/**
	 * The name of the asset, can be a path to the resource, or only a name, extensions can be dropped as well if there is no ambiguity.
	 */
	private String   name;
	/**
	 * The class type of the asset, like Texture, TextureRegion, Skin etc.
	 */
	private Class<T> type;

	/**
	 * Asset gets cached once it is loaded, might be possible to get rid of this.
	 */
	private transient T cachedAsset = null;

	private transient boolean fetched = false;

	/**
	 * Remembers, if the instantiating call resulted in ambiguous assets. Only relevant in development?
	 */
	private transient boolean ambiguous = false;

	/**
	 * Holds references to various placeholders for different asset types, which will be used while assets are loading.
	 */
	private static Map<Class, Object> placeholders;
	/**
	 * Holds references to various placeholders which will be used in case of ambiguity to provide visual cues in. I really hate it if
	 * the program crashes, and I get weird stack traces due to incorrect asset names etc.
	 */
	private static Map<Class, Object> ambiguity;

	/**
	 * Special progress texture which visualizes the asset loading progress on all Textures and TextureRegions.
	 */
	private static Texture progressTexture = null;

	/**
	 * File handle resolver to find the asset files.
	 */
	private static SmartFileHandleResolver resolver;
	/**
	 * The actual asset manager used for the backend asset loading.
	 */
	private static AssetManager manager;

	private static boolean initialized = false;

	/**
	 * This initialization needs to be called before the asset class can be used to set up everything properly.
	 */
	public static void initialize() {
		if (!initialized) {
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
			initialized = true;
		}
	}

	/**
	 * Call this at the very and of a program to clean up all the demons we called in the previous method.
	 */
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
		if (progressTexture != null) {
			progressTexture.dispose();
		}
	}

	/**
	 * Static method to get a new asset object.
	 * @param name The name or path of the requested asset.
	 * @param type The type in which the asset should been loaded.
	 * @return A new instance of the asset, contains the requested asset or a placeholder if the loading process is still not done.
	 */
	public static <A> Asset<A> fetch(String name, Class<A> type) {
		SmartFileHandleResolver.NAME_CHECK check = resolver.check(name);
		Asset<A> asset = new Asset<>(name, type);
		if (check == SmartFileHandleResolver.NAME_CHECK.EXISTS) {
			if (!manager.isLoaded(name, type)) {
				manager.load(name, type);
			}
		}
		if (check == SmartFileHandleResolver.NAME_CHECK.AMBIGUOUS) {
			asset.ambiguous = true;
		}
		asset.fetched = true;
		return asset;
	}

	/**
	 * Gets the actual resource out of the asset. Needs to be called over and over, until the actual resource is ready.
	 * @return The resource, or a placeholder for such.
	 */
	public T get() {
		if (!fetched) {
			SmartFileHandleResolver.NAME_CHECK check = resolver.check(name);
			if (check == SmartFileHandleResolver.NAME_CHECK.EXISTS) {
				if (!manager.isLoaded(name, type)) {
					manager.load(name, type);
				}
			}
			if (check == SmartFileHandleResolver.NAME_CHECK.AMBIGUOUS) {
				ambiguous = true;
			}
			fetched = true;
		}
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
				}
				return (T) placeholders.get(type);
			}
		}
		return cachedAsset;
	}

	/**
	 * Checks if the resource currently provided is still a placeholder and not the real thing.
	 */
	public boolean isPlaceholder() {
		return cachedAsset == null;
	}

	/**
	 * Private constructor
	 */
	private Asset(String name, Class<T> type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Gets the asset manager for the assets.
	 * @return The used asset manager.
	 */
	public static AssetManager getManager() {
		return manager;
	}

	/**
	 * File handle resolver, who doesn't really care if the requested file is specified with its proper path. If that path is missing the
	 * handler simply goes on a small hunt for the file through all files in the working directory. There he will check the name against
	 * all file names with or without extension and partial paths will be considered too. Yeah I really like it if the code can correct
	 * all my laziness.
	 */
	private static class SmartFileHandleResolver
			implements FileHandleResolver {

		/**
		 * Some constants to indicate the result of some name hunt.
		 */
		public static enum NAME_CHECK {
			EXISTS,
			MISSING,
			AMBIGUOUS
		}

		/**
		 * Looks for the specified file.
		 * @param fileName The path, name, name.ext, partial path what ever you like.
		 * @return A file handle for the specified file.
		 */
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

		/**
		 * Will determine, if the file can be found, or not, or if the name can match several files at once.
		 * @param fileName The file to look for.
		 * @return If the file can be found, is missing, or ambiguous.
		 */
		public NAME_CHECK check(String fileName) {
			if (fileName.equals("")) {
				return NAME_CHECK.MISSING;
			}
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

		/**
		 * Searches through all subdirectories recursively to find the file.
		 * @param origin The current directory.
		 * @param name The name of the requested file.
		 * @return The file handle to the file, if any.
		 */
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

		/**
		 * Same as findFile(), however does not stop after the first matching file, but gives back all of the.
		 * @param origin The current directory.
		 * @param name The name of the file.
		 * @return All files found.
		 */
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

}
