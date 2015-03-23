package com.the.machine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.the.machine.components.AgentComponent;
import com.the.machine.components.AreaComponent;
import com.the.machine.components.SprintComponent;
import com.the.machine.components.VisionComponent;
import com.the.machine.framework.IteratingSystem;
import com.the.machine.framework.components.TransformComponent;
import com.the.machine.framework.utility.EntityUtilities;
import com.the.machine.scenes.MapEditorSceneBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 23/03/15
 */
public class VictorySystem extends IteratingSystem {

	transient private ComponentMapper<AgentComponent> agents = ComponentMapper.getFor(AgentComponent.class);
	transient private ComponentMapper<VisionComponent> visions = ComponentMapper.getFor(VisionComponent.class);
	transient private ComponentMapper<SprintComponent> sprints = ComponentMapper.getFor(SprintComponent.class);

	transient private List<VictoryCommand> commands = new ArrayList<>();

	public VictorySystem() {
		super(Family.all(AgentComponent.class)
					.get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		if (sprints.has(entity)) {
			AgentComponent agentComponent = agents.get(entity);
			VictoryCommand foundCommand = null;
			for (VictoryCommand command : commands) {
				if (command.victor.get() == entity) {
					foundCommand = command;
					break;
				}
			}
			if (foundCommand != null) {
				foundCommand.time += deltaTime;
				if (foundCommand.time > 3) {
					MapEditorSceneBuilder.toggleSimulationSystems(world, false);
					System.out.println("Intruders won!");
				}
			}
			if (agentComponent.getEnvironmentType() == AreaComponent.AreaType.TARGET && foundCommand == null) {
				commands.add(new VictoryCommand(new WeakReference<>(entity), 0));
			}
		} else {
			List<WeakReference<Entity>> visibleAgents = visions.get(entity)
															   .getVisibleAgents();
			for (WeakReference<Entity> agent : visibleAgents) {
				Entity agentEntity = agent.get();
				if (agentEntity != null && sprints.has(agentEntity)) {
					TransformComponent atf = EntityUtilities.computeAbsoluteTransform(agentEntity);
					TransformComponent tf = EntityUtilities.computeAbsoluteTransform(entity);
					float dst2 = atf.getPosition()
									.dst2(tf.getPosition());
					if (dst2 < 1.5 * 1.5) {
						MapEditorSceneBuilder.toggleSimulationSystems(world, false);
						System.out.println("Guards won!");
					}
				}
			}
		}
	}

	@AllArgsConstructor
	@Data
	private static class VictoryCommand {
		WeakReference<Entity> victor;
		float time;
	}
}
