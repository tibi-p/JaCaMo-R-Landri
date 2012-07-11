package org.aria.rlandri.generic.artifacts;

import cartago.OPERATION;
import cartago.OpFeedbackParam;

abstract public class RealTimeMultiPlayerCoordinator extends Coordinator {

	@Override
	@OPERATION
	void registerAgent(OpFeedbackParam<String> wsp) throws Exception {
		super.registerAgent(null);
		wsp.set("NA");
	}

}
