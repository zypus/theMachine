package com.the.machine.framework.components;

import com.badlogic.ashley.core.Entity;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 19/02/15
 */
public interface ComponentChangeListener {

	void componentChanged(ObservableComponent component, Entity owner, String value);

}
