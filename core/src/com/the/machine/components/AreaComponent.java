package com.the.machine.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Filter;
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

	// CATEGORIES
	public static final short STRUCTURE_CATEGORY = 0x0001;
	public static final short WALKABLE_CATEGORY = 0x0002;
	public static final short AGENT_CATEGORY = 0x0004;
	public static final short TRANSPARENT_CATEGORY = 0x0008;

	// MASKS
	public static final short WALKABLE_AREA_MASK = STRUCTURE_CATEGORY | TRANSPARENT_CATEGORY | WALKABLE_CATEGORY;
	public static final short STRUCTURE_AREA_MASK = STRUCTURE_CATEGORY | TRANSPARENT_CATEGORY | WALKABLE_CATEGORY | AGENT_CATEGORY;
	public static final short AGENT_MASK = STRUCTURE_CATEGORY | TRANSPARENT_CATEGORY;
	public static final short LIGHT_MASK = STRUCTURE_CATEGORY;

	private AreaType type;

	public AreaComponent setType(AreaType type) {
		this.type = type;
		dirty = true;
		return this;
	}

	public static enum AreaType {
		GROUND(Asset.fetch("floor", TextureRegion.class), WALKABLE_CATEGORY, WALKABLE_AREA_MASK, (short) 0),
		WALL(Asset.fetch("wall", TextureRegion.class), STRUCTURE_CATEGORY, STRUCTURE_AREA_MASK, (short) 0),
		WINDOW(Asset.fetch("window.jpg", TextureRegion.class), TRANSPARENT_CATEGORY, STRUCTURE_AREA_MASK, (short) 0),
		WINDOW_BROKEN(Asset.fetch("window_broken", TextureRegion.class), WALKABLE_CATEGORY, WALKABLE_AREA_MASK, (short) 0),
		DOOR_OPEN(Asset.fetch("door_open", TextureRegion.class), WALKABLE_CATEGORY, WALKABLE_AREA_MASK, (short) 0),
		DOOR_CLOSED(Asset.fetch("door_closed", TextureRegion.class), STRUCTURE_CATEGORY, STRUCTURE_AREA_MASK, (short) 0),
		TARGET(Asset.fetch("target", TextureRegion.class), WALKABLE_CATEGORY, WALKABLE_AREA_MASK, (short) 0),
		TOWER(Asset.fetch("tower", TextureRegion.class), STRUCTURE_CATEGORY, STRUCTURE_AREA_MASK, (short) 0),
		TOWER_USED(Asset.fetch("", TextureRegion.class), STRUCTURE_CATEGORY, STRUCTURE_AREA_MASK, (short) 0),
		COVER(Asset.fetch("", TextureRegion.class), WALKABLE_CATEGORY, WALKABLE_AREA_MASK, (short) 0);

		private Asset<TextureRegion> textureRegionAsset;

		private Filter filter;

		public Asset<TextureRegion> getTextureAsset() {
			return textureRegionAsset;
		}

		public Filter getFilter() {
			return filter;
		}

		private AreaType(Asset<TextureRegion> textureRegionAsset, short categoryBits, short maskBits, short groupIndex) {
			this.textureRegionAsset = textureRegionAsset;
			this.filter = new Filter();
			this.filter.categoryBits = categoryBits;
			this.filter.maskBits = maskBits;
			this.filter.groupIndex = groupIndex;
		}

	}

}
