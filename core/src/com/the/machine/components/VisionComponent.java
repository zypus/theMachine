package com.the.machine.components;

import com.badlogic.ashley.core.Entity;
import com.the.machine.framework.components.AbstractComponent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 20/03/15
 */
@Getter
@NoArgsConstructor
@Accessors(chain = true)
public class VisionComponent extends AbstractComponent {
	private float minDistance = 0;
	private float maxDistance = 6;
	private float angle = 45;

	boolean blind = false;

	@Setter private List<DiscreteMapComponent.MapCell> visibleCells = new ArrayList<>();
	@Setter private List<WeakReference<Entity>> visibleAgents = new ArrayList<>();
	@Setter private List<WeakReference<Entity>> visibleMarkers = new ArrayList<>();

	public VisionComponent setMinDistance(float minDistance) {
		if (this.minDistance != minDistance) {
			setChanged();
		}
		this.minDistance = minDistance;
		return this;
	}

	public VisionComponent setMaxDistance(float maxDistance) {
		if (this.maxDistance != maxDistance) {
			setChanged();
		}
		this.maxDistance = maxDistance;
		return this;
	}

	public VisionComponent setAngle(float angle) {
		if (this.angle != angle) {
			setChanged();
		}
		this.angle = angle;
		return this;
	}

	public VisionComponent setBlind(boolean blind) {
		if (this.blind != blind) {
			setChanged();
		}
		this.blind = blind;
		return this;
	}
}
