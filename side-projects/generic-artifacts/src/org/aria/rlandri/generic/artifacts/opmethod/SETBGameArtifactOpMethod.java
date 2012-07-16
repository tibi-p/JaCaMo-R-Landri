package org.aria.rlandri.generic.artifacts.opmethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.aria.rlandri.generic.artifacts.EnvStatus;
import org.aria.rlandri.generic.artifacts.SimultaneouslyExecutedCoordinator;

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

	// TODO (mihai) check if running
	public void exec(Object[] actualParams) throws Exception {
		try{
		invokeParameterless("preliminaryCheck");
		String msgFmt = "%s: saving execution with parameters %s";
		logger.debug(String.format(msgFmt, this, Arrays.toString(actualParams)));
		if (coordinator instanceof SimultaneouslyExecutedCoordinator) {
			SimultaneouslyExecutedCoordinator seCoordinator = (SimultaneouslyExecutedCoordinator) coordinator;
			boolean isLast = seCoordinator.waitForEndTurn();
			try {
				validate(coordinator, actualParams);
				super.exec(actualParams);
			} finally {
				if (isLast)
					coordinator.setState(EnvStatus.RUNNING);
			}
		}
		} catch (InvocationTargetException e) {
			throw e;
		} catch (Exception e) {
			System.err.println("**** UNCANNY EXCEPTION ****");
			e.printStackTrace(System.err);
		}
	}

	protected void preliminaryCheck() {
		coordinator.failIfNotRunning();
	}

}
