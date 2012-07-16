package org.aria.rlandri.generic.artifacts.opmethod;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.aria.rlandri.generic.artifacts.Coordinator;

public class MasterArtifactOpMethod extends ValidatorArtifactOpMethod {

	private static final Logger logger = Logger
			.getLogger(MasterArtifactOpMethod.class);

	public MasterArtifactOpMethod(Coordinator coordinator, Method method,
			Method validatorMethod) {
		super(coordinator, method, validatorMethod);
	}

	public void exec(Object[] actualParams) throws Exception {
		String msgFmt = "%s: trying to execute as master with parameters %s";
		logger.debug(String.format(msgFmt, this, Arrays.toString(actualParams)));
		invokeParameterless("preliminaryCheck");
		validate(coordinator, actualParams);
		super.exec(actualParams);
	}

	protected void preliminaryCheck() {
		coordinator.failIfNotRegisteredMasterAgent();
	}

}
