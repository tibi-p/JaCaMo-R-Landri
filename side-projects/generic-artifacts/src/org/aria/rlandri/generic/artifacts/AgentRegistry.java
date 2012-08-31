package org.aria.rlandri.generic.artifacts;

import java.util.HashSet;
import java.util.Set;

import cartago.AgentId;

/**
 * A registry of agents. Keeps track of both registered and unregistered agents.
 * 
 * @author Tiberiu Popa
 */
public class AgentRegistry {

	/**
	 * The set of agents that are expected to register, but haven't done so yet.
	 */
	private final Set<String> agentNames = new HashSet<String>();
	/**
	 * The set of agents that have already registered.
	 */
	private final Set<AgentId> agentIds = new HashSet<AgentId>();

	/**
	 * Returns the <tt>Set</tt> of unregistered agents.
	 * 
	 * @return the <tt>Set</tt> of unregistered agents
	 */
	public Set<String> getUnregisteredAgentNames() {
		return agentNames;
	}

	/**
	 * Returns the <tt>Set</tt> of registered agents.
	 * 
	 * @return the <tt>Set</tt> of registered agents
	 */
	public Set<AgentId> getAgentIds() {
		return agentIds;
	}

	/**
	 * Returns the number of registered agents.
	 * 
	 * @return the number of registered agents
	 */
	public int getNumRegistered() {
		return agentIds.size();
	}

	/**
	 * Returns <tt>true</tt> if the agent is registered.
	 * 
	 * @param agentId
	 *            agent whose registration status is to be tested
	 * @return <tt>true</tt> if the agent is registered
	 */
	public boolean isRegistered(AgentId agentId) {
		return agentIds.contains(agentId);
	}

	/**
	 * Adds an agent to the pool of unregistered agents.
	 * 
	 * @param agentName
	 *            the name of the agent to be added
	 */
	public void addAgentName(String agentName) {
		agentNames.add(agentName);
	}

	/**
	 * Adds an agent with multiple personalities to the pool of unregistered
	 * agents.
	 * 
	 * @param agentName
	 *            the name of the agent to be added
	 * @param qty
	 *            the number of clones of the agent
	 */
	public void addAgentName(String agentName, int qty) {
		if (qty > 1) {
			for (int i = 1; i <= qty; i++)
				addAgentName(agentName + i);
		} else {
			addAgentName(agentName);
		}
	}

	/**
	 * Register an agent.
	 * 
	 * @param agentId
	 *            the agent to be registered
	 * @return <tt>true</tt> if the agent has successfully registered
	 */
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

	/**
	 * Returns <tt>true</tt> if the unregistered set contains the specified
	 * agent.
	 * 
	 * @param agentName
	 *            the agent whose presence in the unregistered set is to be
	 *            tested
	 * @return <tt>true</tt> if the unregistered set contains the specified
	 *         agent
	 */
	private boolean containsAgentName(String agentName) {
		return agentNames.contains(agentName);
	}

}
