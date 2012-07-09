package org.aria.rlandri.generic.artifacts;

import java.util.ArrayList;

import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;

import cartago.CartagoException;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

abstract public class RealTimeSinglePlayerCoordinator extends Coordinator {

	private int index = 0;

	@OPERATION
	void registerAgent(OpFeedbackParam<String> privateSubenv) throws CartagoException{
		super.init();
	    privateSubenv.set(getOpUserName() + "_private_workspace");
	}

	// TODO @PRIME_AGENT_OPERATION
	@OPERATION
	private void getNextAgent(OpFeedbackParam<String> wspName) {
		if (index == agents.keySet().size())
			wspName.set("no_more");
		else{
			wspName.set(new ArrayList<String>(agents.keySet()).get(index) + "_private_workspace");
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

}
