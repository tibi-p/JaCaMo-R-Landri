package org.aria.rlandri.generic.artifacts;

import org.aria.rlandri.generic.artifacts.Coordinator.EnvStatus;

import cartago.CartagoException;
import cartago.OpFeedbackParam;

abstract public class RealTimeMultiPlayerCoordinator extends Coordinator {

	@Override
	void registerAgent(OpFeedbackParam<String> wsp) throws Exception {
		super.registerAgent(null);
		wsp.set("NA");
	}

	@Override
	void finishSubenv() {
		updateRank();
		updateCurrency();
		saveState();
	}

}
