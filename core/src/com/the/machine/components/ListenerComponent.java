package com.the.machine.components;

import com.badlogic.gdx.math.Vector2;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ListenerComponent extends AbstractComponent {

	boolean deaf = false;
	private List<Vector2> soundDirections = new ArrayList<>();
}
