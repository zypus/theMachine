package com.the.machine.components;

import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MarkerComponent extends AbstractComponent {
	boolean guardMarker;
	int markerNumber;
}
