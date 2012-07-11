package org.aria.rlandri.generic.artifacts;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.aria.rlandri.generic.artifacts.opmethod.ValidatorArtifactOpMethod;

import cartago.CartagoException;

public class PrimeAgentArtifactOpMethod extends ValidatorArtifactOpMethod {

	private static final Logger logger = Logger
			.getLogger(PrimeAgentArtifactOpMethod.class);

	public PrimeAgentArtifactOpMethod(Coordinator coordinator, Method method,
			Method validatorMethod) {
		super(coordinator, method, validatorMethod);
	}

	public void exec(Object[] actualParams) throws Exception {
		String msgFmt = "%s: checking if prime agent is executing with parameters %s";
		logger.debug(String.format(msgFmt, this, Arrays.toString(actualParams)));
		if (!coordinator.isPrimeAgent()) {
			String errFmt = "Only the prime agent can execute %s";
			throw new CartagoException(String.format(errFmt, this));
		}
		validate(coordinator, actualParams);
		super.exec(actualParams);
	}

}
