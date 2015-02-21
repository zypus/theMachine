package com.the.machine.framework.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 18/02/15
 */
public class TextureRegionLoader
		extends AsynchronousAssetLoader<TextureRegion, TextureRegionLoader.TextureRegionLoaderParameters> {

	/**
	 * Constructor, sets the {@link com.badlogic.gdx.assets.loaders.FileHandleResolver} to use to resolve the file associated with the asset name.
	 *
	 * @param resolver
	 */
	public TextureRegionLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	TextureRegion textureRegion;

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, TextureRegionLoaderParameters parameter) {
		textureRegion = null;
		TextureAtlas.AtlasRegion region = null;
		FileHandle atlasHandle = Gdx.files.local("atlases/assets.atlas");
		if (atlasHandle.exists()) {
			TextureAtlas atlas = manager.get("assets.atlas", TextureAtlas.class);
			String[] withoutExt = fileName.split("\\.");
			String[] comps = withoutExt[0].split("/");
			region = atlas.findRegion(comps[comps.length-1]);
		}
		if (region != null) {
			textureRegion = region;
		} else {
			Texture texture = manager.get(fileName, Texture.class);
			textureRegion = new TextureRegion(texture);
		}
	}

	@Override
	public TextureRegion loadSync(AssetManager manager, String fileName, FileHandle file, TextureRegionLoaderParameters parameter) {
		TextureRegion region = textureRegion;
		textureRegion = null;
		return region;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, TextureRegionLoaderParameters parameter) {
		FileHandle atlas = Gdx.files.local("atlases/assets.atlas");
		Array<AssetDescriptor> descriptors = new Array<>();
		if (atlas.exists()) {
			descriptors.add(new AssetDescriptor("assets.atlas", TextureAtlas.class));
		} else {
			descriptors.add(new AssetDescriptor(fileName, Texture.class));
		}
		return descriptors;
	}

	public static class TextureRegionLoaderParameters extends AssetLoaderParameters<TextureRegion> {

	}

}
