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
 * @created 01/03/15
 */
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class InspectorComponent extends Component {
	transient private WeakReference<Entity> subject;
}
