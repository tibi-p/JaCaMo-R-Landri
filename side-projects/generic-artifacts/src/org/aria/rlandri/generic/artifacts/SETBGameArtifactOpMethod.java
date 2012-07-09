package org.aria.rlandri.generic.artifacts;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.log4j.Logger;

import cartago.ArtifactOpMethod;

public class SETBGameArtifactOpMethod extends ArtifactOpMethod {

	private static final Logger logger = Logger
			.getLogger(SETBGameArtifactOpMethod.class);

	private SimultaneouslyExecutedCoordinator coordinator;
	private Object[] actualParams;

	public SETBGameArtifactOpMethod(
			SimultaneouslyExecutedCoordinator coordinator, Method method) {
		super(coordinator, method);
		this.coordinator = coordinator;
	}

	public void execSavedParameters() throws Exception {
		String msgFmt = "%s: executing op using saved parameters %s";
		logger.debug(String.format(msgFmt, this, Arrays.toString(actualParams)));
		if (actualParams != null)
			super.exec(actualParams);
	}

	// TODO (andrei) check whose turn it is
	// TODO (mihai) check if running
	public void exec(Object[] actualParams) throws Exception {
		coordinator.failIfNotRunning();
		String msgFmt = "%s: saving execution with parameters %s";
		logger.debug(String.format(msgFmt, this, Arrays.toString(actualParams)));
		this.actualParams = actualParams;
		coordinator.addOpMethod(this);
	}

}
