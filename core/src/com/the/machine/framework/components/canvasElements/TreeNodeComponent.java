package com.the.machine.framework.components.canvasElements;

import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Getter;
import lombok.Setter;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/02/15
 */
@Getter
public class TreeNodeComponent extends AbstractComponent {
	@Setter Tree.Node node = null;
	@Setter private boolean added = false;
}
