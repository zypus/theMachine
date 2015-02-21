package com.the.machine.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	private Entity parent;
	private int index;

	public <T extends Component> T askForNext(Class<T> type, ComponentMapper<T> componentMapper, ComponentMapper<ParentComponent> parents) {
		if (componentMapper.has(parent)) {
			return componentMapper.get(parent);
		} else {
			if (parents.has(parent)) {
				return parents.get(parent)
									 .askForNext(type, componentMapper, parents);
			} else {
				return null;
			}
		}
	}

	public <T extends Component> T askForTopmost(Class<T> type, ComponentMapper<T> componentMapper, ComponentMapper<ParentComponent> parents) {
		T topMost = null;
		if (componentMapper.has(parent)) {
			topMost =  componentMapper.get(parent);
		}
		if (parents.has(parent)) {
			T moreTopMost = parents.get(parent)
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
