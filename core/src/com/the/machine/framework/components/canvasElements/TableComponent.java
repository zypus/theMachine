package com.the.machine.framework.components.canvasElements;

import com.the.machine.framework.components.AbstractComponent;
import com.the.machine.framework.utility.Enums;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/02/15
 */
@Data
@Accessors(chain = true)
public class TableComponent extends AbstractComponent {
	// TODO add table related stuff
	private boolean fillParent = false;
	private boolean debug = false;
	private Enums.HorizontalAlignment horizontalAlignment = Enums.HorizontalAlignment.CENTER;
	private Enums.VerticalAlignment   verticalAlignment   = Enums.VerticalAlignment.CENTER;
}
