package com.the.machine.components;

import com.badlogic.gdx.math.Vector2;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DiscreteMapComponent extends AbstractComponent {

	List<MapCell> sparseMap = new ArrayList<>();

	@Data
	@NoArgsConstructor
	@Accessors(chain = true)
	public static class MapCell {
		Vector2 position;
		AreaComponent.AreaType type = AreaComponent.AreaType.GROUND;

		public MapCell(MapCell mapCell) {
			this.position = mapCell.position.cpy();
			this.type = mapCell.getType();
		}
	}
}
