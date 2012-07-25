package org.aria.rlandri.generic.artifacts;

import java.util.HashSet;
import java.util.Set;

import cartago.AgentId;

public class AgentRegistry {

	private final Set<String> agentNames = new HashSet<String>();
	private final Set<AgentId> agentIds = new HashSet<AgentId>();

	public Set<String> getUnregisteredAgentNames() {
		return agentNames;
	}

	public Set<AgentId> getAgentIds() {
		return agentIds;
	}

	public int getNumRegistered() {
		return agentIds.size();
	}

	public boolean isRegistered(AgentId agentId) {
		return agentIds.contains(agentId);
	}

	public void addAgentName(String agentName) {
		agentNames.add(agentName);
	}

	public void addAgentName(String agentName, int qty) {
		if (qty > 1) {
			for (int i = 1; i <= qty; i++)
				addAgentName(agentName + i);
		} else {
			addAgentName(agentName);
		}
	}

	public boolean registerAgent(AgentId agentId) {
		String agentName = agentId.getAgentName();
		if (containsAgentName(agentName)) {
			agentIds.add(agentId);
			agentNames.remove(agentName);
			return true;
		} else {
			return false;
		}
	}

	private boolean containsAgentName(String agentName) {
		return agentNames.contains(agentName);
	}

}
