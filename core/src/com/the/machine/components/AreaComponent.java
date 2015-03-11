package com.the.machine.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 04/03/15
 */
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class AreaComponent extends AbstractComponent{

	private AreaType type;

	public AreaComponent setType(AreaType type) {
		this.type = type;
		dirty = true;
		return this;
	}

	public static enum AreaType {
		GROUND(Asset.fetch("floor", TextureRegion.class)),
		WALL(Asset.fetch("wall", TextureRegion.class)),
		WINDOW(Asset.fetch("", TextureRegion.class)),
		WINDOW_BROKEN(Asset.fetch("", TextureRegion.class)),
		DOOR_OPEN(Asset.fetch("", TextureRegion.class)),
		DOOR_CLOSED(Asset.fetch("", TextureRegion.class)),
		TARGET(Asset.fetch("", TextureRegion.class)),
		TOWER(Asset.fetch("", TextureRegion.class)),
		TOWER_USED(Asset.fetch("", TextureRegion.class)),
		COVER(Asset.fetch("", TextureRegion.class));

		private Asset<TextureRegion> textureRegionAsset;

		public Asset<TextureRegion> getTextureAsset() {
			return textureRegionAsset;
		}

		private AreaType(Asset<TextureRegion> textureRegionAsset) {
			this.textureRegionAsset = textureRegionAsset;
		}

	}

}
