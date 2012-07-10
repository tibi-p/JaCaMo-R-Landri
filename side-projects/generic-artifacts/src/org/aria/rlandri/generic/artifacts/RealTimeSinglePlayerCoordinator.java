package org.aria.rlandri.generic.artifacts;

import java.util.ArrayList;
import java.util.Collections;

import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;

import cartago.CartagoException;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class RealTimeSinglePlayerCoordinator extends Coordinator {

	private int index = 0;
	private String last;

	@OPERATION
	void registerAgent(OpFeedbackParam<String> privateSubenv) throws Exception {
		super.registerAgent(null);
		last = "";
		String agentName = getOpUserName();
		if (agentName.matches(".+_[0-9]")) {
			privateSubenv.set(agentName.substring(0,
					agentName.lastIndexOf('_') + 1) + "private_workspace");
		} else
			privateSubenv.set(getOpUserName() + "_private_workspace");
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
						agents.get(index).lastIndexOf('_'));
			else {
				wspName.set(agents.get(index) + "_private_workspace");
				index++;
				return;
			}
			// System.out.println(root);
			while (root.equals(last)) {
				// System.out.println(root);
				index++;
				if (index == agents.size()) {
					wspName.set("no_more");
					return;
				}
				if (agents.get(index).matches(".+_[0-9]"))
					root = agents.get(index).substring(0,
							agents.get(index).lastIndexOf('_'));
				else
					root = agents.get(index);
			}
			last = root;
			wspName.set(root + "private_workspace");
			index++;
		}
	}
	
	@PRIME_AGENT_OPERATION
	void checkStatus(OpFeedbackParam<String> status) {
		
	}

	@Override
	protected void fillOperations() throws CartagoException {
		addOperation(new CoordinatorAnnotation(GAME_OPERATION.class,
				RTSPGameArtifactOpMethod.class, true));
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

