package com.the.machine.map;

import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AreaComponent;
import com.the.machine.systems.VisionSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 12/05/15
 */
public class Mapper {
	List<List<AreaComponent.AreaType>> map = new ArrayList<>();
	Vector2 currentPosition;
	Vector2 currentDirection;
	int width = 0;
	int height = 0;

	public void init(Vector2 direction) {
		currentPosition = new Vector2(0, 0);
		currentDirection = direction.cpy().nor();
	}

	public void update(Vector2 direction, float currentSpeed, float deltaTime, float visionAngle, float visionRange, List<VisionSystem.EnvironmentVisual> visuals) {
		currentPosition.add(currentDirection.scl(currentSpeed*deltaTime));
		currentDirection = direction.cpy().nor();
	}
}
