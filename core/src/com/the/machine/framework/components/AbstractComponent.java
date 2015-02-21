package com.the.machine.framework.components;

import lombok.Data;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 16/02/15
 */
@Data
public abstract class AbstractComponent extends ObservableComponent {
	protected boolean enabled = true;
	protected transient       boolean dirty   = true;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
