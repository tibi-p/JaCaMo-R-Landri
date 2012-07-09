package org.aria.rlandri.generic.artifacts;

import java.util.ArrayList;
import java.util.Collections;

import cartago.OPERATION;
import cartago.OpFeedbackParam;

abstract public class RealTimeSinglePlayerCoordinator extends Coordinator {

	private int index = 0;
	private String last;

	@OPERATION
	void registerAgent(OpFeedbackParam<String> privateSubenv) throws Exception{
		super.registerAgent(null);
		last = "";
		String agentName = getOpUserName();
		if(agentName.matches(".+_[0-9]")){
			privateSubenv.set(agentName.substring(0, agentName.lastIndexOf('_') + 1) + "private_workspace");
		}
		else privateSubenv.set(getOpUserName() + "_private_workspace");
	}

	// TODO @PRIME_AGENT_OPERATION
	@OPERATION
	private void getNextAgent(OpFeedbackParam<String> wspName) {
		ArrayList<String> agents = new ArrayList<String>(super.agents.keySet());
		Collections.sort(agents);
		if (index == agents.size())
			wspName.set("no_more");
		else {
			String root;
			if(agents.get(index).matches(".+_[0-9]")) root = agents.get(index).substring(0, agents.get(index).lastIndexOf('_'));
			else{
				wspName.set(agents.get(index) + "_private_workspace");
				index++;
				return;
			}
			//System.out.println(root);
			while(root.equals(last)) {
				//System.out.println(root);
				index++;
				if(index == agents.size()) {
					wspName.set("no_more");
					return;
				}
				if(agents.get(index).matches(".+_[0-9]"))root = agents.get(index).substring(0, agents.get(index).lastIndexOf('_'));
				else root = agents.get(index);
			}
			last = root;
			wspName.set(root + "private_workspace");
			index++;
		}
	}

	abstract void registerOperations();

	// TODO @PRIME_AGENT_OPERATION
	@OPERATION
	void checkStatus(OpFeedbackParam<String> status) {
	}

	@OPERATION
	private void finishEnv(){
		updateRank();
		updateCurrency();
		saveState();
	}

	// TODO @PRIME_AGENT_OPERATION
	@OPERATION
	void startSubenv() {
		signal("startSubenv");
		state = EnvStatus.RUNNING;
	}

	@Override
	void finishSubenv() {
		updateRank();
		updateCurrency();
		saveState();
	}

}
