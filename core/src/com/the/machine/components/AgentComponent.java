package com.the.machine.components;

import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/03/15
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AgentComponent
		extends AbstractComponent {
	AreaComponent.AreaType environmentType = AreaComponent.AreaType.GROUND;
	float goalAngle = 0;
	float angularSpeed = 0;
	float visionModifier = 1;
	float viewingAngle = 45;
	float maxMovementSpeed = 1.4f;
	float maxTurningSpeed = 180;
	boolean hidden = false;
	boolean inTower = false;
	boolean acting = false;

	public AgentComponent setVisionModifier(float mod) {
		if (visionModifier != mod) {
			setChanged();
		}
		visionModifier = mod;
		return this;
	}

	float baseViewingDistance = 6.5f;
	float baseViewingAngle = 45;
	float baseMovementSpeed = 1.4f;
	float baseMaxTurningSpeed = 180;
}
