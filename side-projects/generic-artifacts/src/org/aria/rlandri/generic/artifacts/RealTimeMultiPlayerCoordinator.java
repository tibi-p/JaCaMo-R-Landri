package org.aria.rlandri.generic.artifacts;

import cartago.OpFeedbackParam;

abstract public class RealTimeMultiPlayerCoordinator extends Coordinator {

	@Override
	void registerAgent(OpFeedbackParam<String> wsp) throws Exception {
		super.registerAgent(null);
		wsp.set("NA");
	}

}
