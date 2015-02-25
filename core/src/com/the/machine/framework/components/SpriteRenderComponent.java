package com.the.machine.framework.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.systems.rendering.LayerSortable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The SpriteRenderComponent is added to sprites which can be viewed by a camera
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/02/15
 */
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class SpriteRenderComponent
		extends AbstractComponent implements LayerSortable {
	private String sortingLayer = null;
	private int sortingOrder = 0;
	private Asset<TextureRegion>
	textureRegion;
	private Color tint = Color.WHITE;

	public SpriteRenderComponent setTextureRegion(Asset<TextureRegion> textureRegion) {
		this.textureRegion = textureRegion;
		notifyObservers();
		return this;
	}

	public SpriteRenderComponent setTint(Color tint) {
		this.tint = tint;
		notifyObservers();
		return this;
	}

	public boolean isDirty() {
		if (dirty) {
			dirty = false;
			return true;
		}
		return false;
	}

}
