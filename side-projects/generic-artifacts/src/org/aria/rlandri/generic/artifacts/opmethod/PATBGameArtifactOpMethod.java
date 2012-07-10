package org.aria.rlandri.generic.artifacts.opmethod;

import java.lang.reflect.Method;

import org.aria.rlandri.generic.artifacts.PlayerAlternatedCoordinator;

public class PATBGameArtifactOpMethod extends ValidatorArtifactOpMethod {

	public PATBGameArtifactOpMethod(PlayerAlternatedCoordinator coordinator,
			Method method, Method validatorMethod) {
		super(coordinator, method, validatorMethod);
	}

	public void exec(Object[] actualParams) throws Exception {
		coordinator.failIfNotRunning();
		if (coordinator instanceof PlayerAlternatedCoordinator) {
			PlayerAlternatedCoordinator paCoordinator = (PlayerAlternatedCoordinator) coordinator;
			paCoordinator.failIfNotCurrentTurn();
		}
		validate(coordinator, actualParams);
		super.exec(actualParams);
	}

}
