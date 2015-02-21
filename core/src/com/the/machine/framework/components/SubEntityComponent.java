package com.the.machine.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 11/02/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubEntityComponent extends Component implements Iterable<Entity> {
	@Delegate(excludes = Custom.class) private List<Entity> subEntities = new ArrayList<>();

	public Entity remove(int index) {
		Entity entity = subEntities.remove(index);
		ComponentMapper<ParentComponent> parents = ComponentMapper.getFor(ParentComponent.class);
		for (int i = index; i < size(); i++) {
			Entity e = get(i);
			parents.get(e).setIndex(i);
		}
		return entity;
	}

	private interface Custom {
		Entity remove(int index);
	}

}
