package org.aria.rlandri.generic.artifacts.opmethod;

import java.lang.reflect.Method;

import org.aria.rlandri.generic.artifacts.PlayerAlternatedCoordinator;

public class PATBGameArtifactOpMethod extends ValidatorArtifactOpMethod {

	public PATBGameArtifactOpMethod(PlayerAlternatedCoordinator coordinator,
			Method method, Method validatorMethod) {
		super(coordinator, method, validatorMethod);
	}

	public void exec(Object[] actualParams) throws Exception {
		invokeParameterless("preliminaryCheck");
		if (coordinator instanceof PlayerAlternatedCoordinator) {
			PlayerAlternatedCoordinator paCoordinator = (PlayerAlternatedCoordinator) coordinator;
			paCoordinator.prepareEvaluation();
			try {
				validate(actualParams);
				super.exec(actualParams);
			} finally {
				paCoordinator.resetTurnInfo();
			}
		}
	}

	protected void preliminaryCheck() {
		coordinator.failIfNotRunning();
		coordinator.failIfNotRegisteredParticipatingAgent();
		if (coordinator instanceof PlayerAlternatedCoordinator) {
			PlayerAlternatedCoordinator paCoordinator = (PlayerAlternatedCoordinator) coordinator;
			paCoordinator.failIfNotCurrentTurn();
		}
	}

}
