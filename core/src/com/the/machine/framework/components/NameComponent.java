package com.the.machine.framework.components;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 21/02/15
 */
@Data
@Accessors(chain = true)
public class NameComponent extends ObservableComponent {
	String name = "No Name";
}
