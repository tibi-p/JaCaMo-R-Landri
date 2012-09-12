package org.aria.rlandri.generic.artifacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.opmethod.PrimeAgentArtifactOpMethod;
import org.aria.rlandri.generic.artifacts.opmethod.RTGameArtifactOpMethod;

import cartago.CartagoException;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public abstract class RealTimeSinglePlayerCoordinator extends Coordinator {

	private final Random random = new Random();
	private final Map<String, String> workspaces = new HashMap<String, String>();
	private List<String> agents;
	private int index = 0;
	private String last = null;

	protected void init() throws CartagoException {
		super.init();
		this.agents = new ArrayList<String>(
				regularAgents.getUnregisteredAgentNames());
		Collections.sort(this.agents);
	}

	@OPERATION
	protected void registerAgent(OpFeedbackParam<String> privateSubenv) {
		super.registerAgent(privateSubenv);
		String agentName = getOpUserName();
		agentName = agentName.substring(0, agentName.lastIndexOf('_'));
		privateSubenv.set(workspaces.get(agentName));
	}

	@PRIME_AGENT_OPERATION
	protected void startSubenv() {
		super.startSubenv();
		setState(EnvStatus.RUNNING);
	}

	@PRIME_AGENT_OPERATION
	void getNextAgent(OpFeedbackParam<String> wspName) {
		if (index == agents.size()) {
			wspName.set("no_more");
		} else {
			String agentName = agents.get(index);
			String root = agentName.substring(0, agentName.lastIndexOf('_'));
			if (agentName.endsWith("_")) {
				String name = root + "_private_workspace" + random.nextFloat();
				workspaces.put(root, name);
				wspName.set(name);
				index++;
				return;
			}
			while (root.equals(last)) {
				index++;
				if (index == agents.size()) {
					wspName.set("no_more");
					return;
				}
				agentName = agents.get(index);
				root = agentName.substring(0, agentName.lastIndexOf('_'));
				if (agentName.endsWith("_"))
					break;
			}
			last = root;
			if (workspaces.containsKey(root)) {
				// This should NEVER happen, but we choose the safer side
				wspName.set(workspaces.get(root));
			} else {
				String name = root + "_private_workspace" + random.nextFloat();
				workspaces.put(root, name);
				wspName.set(name);
			}
			index++;
		}
	}

	@PRIME_AGENT_OPERATION
	void checkStatus(OpFeedbackParam<String> status) {

	}

	@Override
	protected void fillOperations() throws CartagoException {
		addOperation(new CoordinatorAnnotation(GAME_OPERATION.class,
				RTGameArtifactOpMethod.class, true));
		addOperation(new CoordinatorAnnotation(PRIME_AGENT_OPERATION.class,
				PrimeAgentArtifactOpMethod.class, false));
	}

}
