package org.aria.rlandri.generic.artifacts;

import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.opmethod.PrimeAgentArtifactOpMethod;
import org.aria.rlandri.generic.artifacts.opmethod.RTGameArtifactOpMethod;

import cartago.CartagoException;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

abstract public class RealTimeMultiPlayerCoordinator extends Coordinator {

	@Override
	@OPERATION
	void registerAgent(OpFeedbackParam<String> wsp) {
		super.registerAgent(wsp);
		wsp.set("NA");
	}

	@PRIME_AGENT_OPERATION
	void getNextAgent(OpFeedbackParam<String> wspName) {
		wspName.set("no_more");
	}

	@Override
	protected void fillOperations() throws CartagoException {
		addOperation(new CoordinatorAnnotation(GAME_OPERATION.class,
				RTGameArtifactOpMethod.class, true));
		addOperation(new CoordinatorAnnotation(PRIME_AGENT_OPERATION.class,
				PrimeAgentArtifactOpMethod.class, false));
	}
}
