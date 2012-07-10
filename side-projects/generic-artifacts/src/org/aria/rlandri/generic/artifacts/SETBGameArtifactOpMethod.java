package org.aria.rlandri.generic.artifacts;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.log4j.Logger;

public class SETBGameArtifactOpMethod extends ValidatorArtifactOpMethod {

	private static final Logger logger = Logger
			.getLogger(SETBGameArtifactOpMethod.class);

	public SETBGameArtifactOpMethod(
			SimultaneouslyExecutedCoordinator coordinator, Method method,
			Method validatorMethod) {
		super(coordinator, method, validatorMethod);
	}

	public void execSavedParameters(Object[] actualParams) throws Exception {
		String msgFmt = "%s: executing op using saved parameters %s";
		logger.debug(String.format(msgFmt, this, Arrays.toString(actualParams)));
		if (actualParams != null)
			super.exec(actualParams);
	}

	// TODO (andrei) check whose turn it is
	// TODO (mihai) check if running
	public void exec(Object[] actualParams) throws Exception {
		coordinator.failIfNotRunning();
		try {
			validate(coordinator, actualParams);
			String msgFmt = "%s: saving execution with parameters %s";
			logger.debug(String.format(msgFmt, this,
					Arrays.toString(actualParams)));
			if (coordinator instanceof SimultaneouslyExecutedCoordinator) {
				SimultaneouslyExecutedCoordinator seCoordinator = (SimultaneouslyExecutedCoordinator) coordinator;
				seCoordinator.addOpMethod(this, actualParams);
			}
		} catch (Exception e) {
			System.err.println("XOXOXO " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}

}
