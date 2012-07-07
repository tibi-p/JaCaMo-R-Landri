package org.aria.rlandri.generic.artifacts;

import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;

import cartago.OPERATION;
import cartago.OpFeedbackParam;

abstract public class RealTimeSinglePlayerCoordinator extends Coordinator {

	private int index = 0;

	@OPERATION
	void registerAgent(OpFeedbackParam<String> privateSubenv){
	    privateSubenv.set(getOpUserName() + "_private_workspace");
	}

	@PRIME_AGENT_OPERATION
	private void getNextAgent(OpFeedbackParam<String> wspName) {
		if (index == participants.size())
			wspName.set("no_more");
		else
			wspName.set(participants.get(index) + "_private_workspace");
	}

	@PRIME_AGENT_OPERATION
	private void incrementIndex() {
		index++;
	}

	abstract void registerOperations();

	@PRIME_AGENT_OPERATION
	void checkStatus(OpFeedbackParam<String> status) {
	}

	@OPERATION
	private void finishEnv(){
		updateRank();
		updateCurrency();
		saveState();
	}

	@PRIME_AGENT_OPERATION
	void startSubenv() {
		signal("startSubenv");
		state = EnvStatus.RUNNING;
	}

}
