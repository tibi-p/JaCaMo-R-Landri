package org.aria.rlandri.generic.artifacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.opmethod.PrimeAgentArtifactOpMethod;
import org.aria.rlandri.generic.artifacts.opmethod.RTGameArtifactOpMethod;

import cartago.CartagoException;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class RealTimeSinglePlayerCoordinator extends Coordinator {

	private int index = 0;
	private String last;
	Map<String, String> workspaces;

	protected void init() throws CartagoException{
		super.init();
		last = "";
		workspaces = new HashMap<String, String>();
	}

	@OPERATION
	void registerAgent(OpFeedbackParam<String> privateSubenv) {
		super.registerAgent(privateSubenv);
		String agentName = getOpUserName();
		if (agentName.matches(".+_[0-9]")) {
			agentName = agentName.substring(0, agentName.lastIndexOf('_'));
			privateSubenv.set(workspaces.get(agentName));
			//System.out.println("for " + agentName + " is " + workspaces.get(agentName));
		} else
			privateSubenv.set(workspaces.get(agentName.substring(0, agentName.lastIndexOf('_'))));
	}

	@PRIME_AGENT_OPERATION
	void getNextAgent(OpFeedbackParam<String> wspName) {
		ArrayList<String> agents = new ArrayList<String>(super.agents.keySet());
		Collections.sort(agents);
		if (index == agents.size())
			wspName.set("no_more");
		else {
			String root;
			if (agents.get(index).matches(".+_[0-9]"))
				root = agents.get(index).substring(0,
						agents.get(index).lastIndexOf('_') - 1);
			else {
				root = agents.get(index);
				root = root.substring(0, root.lastIndexOf('_'));
				String name = root + "_private_workspace" + new Random().nextFloat();
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
				if (agents.get(index).matches(".+_[0-9]"))
					root = agents.get(index).substring(0,
							agents.get(index).lastIndexOf('_') - 1);
				else{
					root = agents.get(index);
					root = root.substring(0, root.lastIndexOf('_'));
					break;
				}
			}
			last = root;
			if(workspaces.containsKey(root))
				wspName.set(workspaces.get(root));
			else{
				String name = root + "_private_workspace" + new Random().nextFloat();
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

	@Override
	protected void updateRank() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateCurrency() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void saveState() {
		// TODO Auto-generated method stub
		
	}

}

