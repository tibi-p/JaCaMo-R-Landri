package org.aria.rlandri.generic.artifacts;

import org.aria.rlandri.generic.artifacts.Coordinator.EnvStatus;

import cartago.OpFeedbackParam;

abstract public class RealTimeMultiPlayerCoordinator extends Coordinator {

	@Override
	void registerAgent(OpFeedbackParam<String> wsp) throws Exception {
		wsp.set("NA");
	}

	@Override
	void startSubenv() throws Exception {
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
