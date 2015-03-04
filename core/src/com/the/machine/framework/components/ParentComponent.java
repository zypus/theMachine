package com.the.machine.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.ref.WeakReference;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/02/15
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ParentComponent extends Component {
	private WeakReference<Entity> parent;
	private int index;

	public <T extends Component> T askForNext(Class<T> type, ComponentMapper<T> componentMapper, ComponentMapper<ParentComponent> parents) {
		Entity parentRef = parent.get();
		if (parentRef != null && componentMapper.has(parentRef)) {
			return componentMapper.get(parentRef);
		} else {
			if (parentRef != null && parents.has(parentRef)) {
				return parents.get(parentRef)
							  .askForNext(type, componentMapper, parents);
			} else {
				return null;
			}
		}
	}

	public <T extends Component> T askForTopmost(Class<T> type, ComponentMapper<T> componentMapper, ComponentMapper<ParentComponent> parents) {
		T topMost = null;
		Entity parentRef = parent.get();
		if (parentRef != null && componentMapper.has(parentRef)) {
			topMost =  componentMapper.get(parentRef);
		}
		if (parentRef != null && parents.has(parentRef)) {
			T moreTopMost = parents.get(parentRef)
								 .askForTopmost(type, componentMapper, parents);
			if (moreTopMost != null) {
				return moreTopMost;
			} else {
				return topMost;
			}
		} else {
			return topMost;
		}
	}

}
