package com.the.machine.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 28/02/15
 */
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class SelectedComponent extends Component {
	WeakReference<Entity> selection;
}
