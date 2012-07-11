package org.aria.rlandri.generic.artifacts.opmethod;

import java.lang.reflect.Method;

import org.aria.rlandri.generic.artifacts.Coordinator;

public class RTSPGameArtifactOpMethod extends ValidatorArtifactOpMethod {

	public RTSPGameArtifactOpMethod(Coordinator coordinator, Method method,
			Method validatorMethod) {
		super(coordinator, method, validatorMethod);
	}
	public void exec(Object[] actualParams) throws Exception {
		coordinator.failIfNotRunning();
		validate(coordinator, actualParams);
		super.exec(actualParams);
	}

}
