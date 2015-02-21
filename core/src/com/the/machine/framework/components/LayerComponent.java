package com.the.machine.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Bits;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 14/02/15
 */
@AllArgsConstructor
@Data
public class LayerComponent extends Component {
	private Bits layer;
}
