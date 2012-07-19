package org.aria.rlandri.generic.artifacts.opmethod;

import java.lang.reflect.Method;

import org.aria.rlandri.generic.artifacts.Coordinator;

public class RTGameArtifactOpMethod extends ValidatorArtifactOpMethod {

	public RTGameArtifactOpMethod(Coordinator coordinator, Method method,
			Method validatorMethod) {
		super(coordinator, method, validatorMethod);
	}

	public void exec(Object[] actualParams) throws Exception {
		coordinator.failIfNotRunning();
		validate(actualParams);
		super.exec(actualParams);
	}

}
