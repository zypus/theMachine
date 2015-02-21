package com.the.machine.framework.events.basic;

import com.the.machine.framework.events.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.Dimension;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 17/02/15
 */
@Data
@AllArgsConstructor
public class ResizeEvent extends Event {
	private Dimension oldSize;
	private Dimension newSize;
}
