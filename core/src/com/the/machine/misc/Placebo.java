package com.the.machine.misc;

import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.DiscreteMapComponent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 22/04/15
 */
@Data
@AllArgsConstructor
public class Placebo {
	Vector2 pos;
	List<DiscreteMapComponent.MapCell> discretizedMap;
}
