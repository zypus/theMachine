package com.the.machine.components;

import com.badlogic.ashley.core.Entity;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class EntryPointComponent extends AbstractComponent {
	WeakReference<Entity> spawned;
}
