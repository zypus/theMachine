package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.BehaviourComponent;
import com.the.machine.components.SprintComponent;
import com.the.machine.events.MarkerEvent;
import com.the.machine.events.ResetEvent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.components.canvasElements.LabelComponent;
import com.the.machine.framework.events.Event;
import com.the.machine.framework.events.EventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 28/05/15
 */
public class GameInfoSystem extends IteratingSystem implements EventListener {

	ComponentMapper<TransformComponent> transforms = ComponentMapper.getFor(TransformComponent.class);
	ComponentMapper<BehaviourComponent> behaviours = ComponentMapper.getFor(BehaviourComponent.class);
	ComponentMapper<SprintComponent> sprints = ComponentMapper.getFor(SprintComponent.class);

	LabelComponent time;
	LabelComponent guard;
	LabelComponent guardMove;
	LabelComponent guardMarker;
	LabelComponent intruder;
	LabelComponent intruderMove;
	LabelComponent intruderMarker;

	Map<Entity, Vector2> moveMap = new HashMap<>();

	float guardMoves    = 0;
	float intruderMoves = 0;

	float pasttime = 0;

	int guardMarkers = 0;
	int intruderMarkers = 0;

	public GameInfoSystem(LabelComponent time, LabelComponent guard, LabelComponent guardMove, LabelComponent guardMarker, LabelComponent intruder, LabelComponent intruderMove, LabelComponent intruderMarker) {
		super(Family.all(AgentComponent.class, TransformComponent.class, BehaviourComponent.class)
					.get());
		this.time = time;
		this.guard = guard;
		this.guardMove = guardMove;
		this.guardMarker = guardMarker;
		this.intruder = intruder;
		this.intruderMove = intruderMove;
		this.intruderMarker = intruderMarker;
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof MarkerEvent) {
			if (((MarkerEvent) event).isGuardMarker()) {
				guardMarkers++;
				guardMarker.setText("-> Markers: "+guardMarkers);
				guardMarker.setDirty(true);
			}else {
				intruderMarkers++;
				intruderMarker.setText("-> Markers: "+intruderMarkers);
				intruderMarker.setDirty(true);
			}
		} else if (event instanceof ResetEvent) {
			pasttime = 0;
			guardMoves = 0;
			intruderMoves = 0;
			guardMarkers = 0;
			intruderMarkers = 0;
			moveMap.clear();
		}
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		pasttime += deltaTime;
		time.setText("Time : " + ((int) (pasttime * 10)) / 10f + "s");
		time.setDirty(true);
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		if (sprints.has(entity)) {
			intruder.setText("Intruders: " + behaviours.get(entity)
													   .getBehaviour()
													   .getClass()
													   .getSimpleName());
			intruder.setDirty(true);
		} else {
			guard.setText("Guards: " + behaviours.get(entity)
												 .getBehaviour()
												 .getClass()
												 .getSimpleName());
			guard.setDirty(true);
		}
		if (moveMap.containsKey(entity)) {
			if (sprints.has(entity)) {
				intruderMoves += moveMap.get(entity)
										.cpy()
										.sub(transforms.get(entity)
													   .get2DPosition())
										.len();
				intruderMove.setText("-> Movement: " + (int)intruderMoves + "m");
				intruderMove.setDirty(true);
			} else {
				guardMoves += moveMap.get(entity)
									 .cpy()
									 .sub(transforms.get(entity)
													.get2DPosition())
									 .len();
				guardMove.setText("-> Movement: " + (int) guardMoves + "m");
				guardMove.setDirty(true);
			}
		}
		moveMap.put(entity, transforms.get(entity)
										  .get2DPosition());
	}
}
