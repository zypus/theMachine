package com.the.machine.framework.components.canvasElements;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.the.machine.framework.assets.Asset;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 19/02/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SkinComponent extends AbstractComponent {
	Asset<Skin> skin;
}
