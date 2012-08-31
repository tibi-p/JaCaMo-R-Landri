package org.aria.rlandri.generic.artifacts.opmethod;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.aria.rlandri.generic.artifacts.SimultaneouslyExecutedCoordinator;

public class SETBGameArtifactOpMethod extends ValidatorArtifactOpMethod {

	private static final Logger logger = Logger
			.getLogger(SETBGameArtifactOpMethod.class);

	public SETBGameArtifactOpMethod(
			SimultaneouslyExecutedCoordinator coordinator, Method method,
			Method validatorMethod) {
		super(coordinator, method, validatorMethod);
	}

	// TODO (mihai) check if running
	public void exec(Object[] actualParams) throws Exception {
		String msgFmt = "%s: trying to execute with parameters %s";
		logger.debug(String.format(msgFmt, this, Arrays.toString(actualParams)));
		invokeParameterless("preliminaryCheck");
		if (coordinator instanceof SimultaneouslyExecutedCoordinator) {
			SimultaneouslyExecutedCoordinator seCoordinator = (SimultaneouslyExecutedCoordinator) coordinator;
			boolean isLast = seCoordinator.waitForEndTurn();
			try {
				validate(actualParams);
				super.exec(actualParams);
			} finally {
				if (isLast)
					seCoordinator.resetTurnInfo();
			}
		}
	}

	protected void preliminaryCheck() {
		coordinator.failIfNotRunning();
		coordinator.failIfNotRegisteredParticipatingAgent();
		if (coordinator instanceof SimultaneouslyExecutedCoordinator) {
			SimultaneouslyExecutedCoordinator seCoordinator = (SimultaneouslyExecutedCoordinator) coordinator;
			seCoordinator.failIfHasMoved();
		}
	}

}
