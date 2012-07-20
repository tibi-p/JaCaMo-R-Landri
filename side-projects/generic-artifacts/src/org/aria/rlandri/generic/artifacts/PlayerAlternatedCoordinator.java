package org.aria.rlandri.generic.artifacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.opmethod.PATBGameArtifactOpMethod;
import org.aria.rlandri.generic.artifacts.opmethod.PrimeAgentArtifactOpMethod;

import cartago.AgentId;
import cartago.CartagoException;
import cartago.GUARD;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

/**
 * @author Andrei Geacar
 * 
 */

public class PlayerAlternatedCoordinator extends Coordinator {

	protected AgentId currentAgent = null;
	protected int currentIndex = 0;

	private final List<AgentId> order = new ArrayList<AgentId>();
	private final Timer timer = new Timer();
	private TimerTask task = null;
	private int currentStep = 0;
	private boolean stepFinished = true;

	// constants for testing purposes
	public static final int STEPS = 5;
	public static final int TURN_LENGTH = 1000;

	public void resetTurnInfo() {
		stepFinished = true;
	}

	public void failIfNotCurrentTurn() {
		AgentId userId = getOpUserId();
		if (!userId.equals(currentAgent)) {
			// TODO: Standard error messages
			failed("not_your_turn");
		}
	}

	@Override
	protected void fillOperations() throws CartagoException {
		addOperation(new CoordinatorAnnotation(GAME_OPERATION.class,
				PATBGameArtifactOpMethod.class, true));
		addOperation(new CoordinatorAnnotation(PRIME_AGENT_OPERATION.class,
				PrimeAgentArtifactOpMethod.class, false));
	}

	// TODO use status here
	@OPERATION
	void registerAgent(OpFeedbackParam<String> wsp) {
		super.registerAgent(wsp);
		order.add(getOpUserId());
		wsp.set("NA");
	}

	@PRIME_AGENT_OPERATION
	protected void startSubenv() {
		super.startSubenv();
		execInternalOp("runSubEnv");
	}

	@INTERNAL_OPERATION
	void runSubEnv() {
		for (currentStep = 1; currentStep <= STEPS; currentStep++) {
			executeStep();
		}
	}

	@INTERNAL_OPERATION
	void finishTimer() {
		EnvStatus state = getState();
		if (state == EnvStatus.RUNNING) {
			setState(EnvStatus.EVALUATING);
			resetTurnInfo();
		} else {
			// TODO handle me
		}
	}

	private void executeStep() {
		currentIndex = 0;
		for (AgentId agentId : order) {
			currentAgent = agentId;
			startPlayerTurn();
			executePlayerTurn();
			currentIndex++;
		}
	}

	private void startPlayerTurn() {
		setState(EnvStatus.RUNNING);
		stepFinished = false;
		signal(currentAgent, "startTurn", currentStep);
	}

	private void executePlayerTurn() {
		task = new TimerTask() {
			public void run() {
				execInternalOp("finishTimer");
			}
		};
		timer.schedule(task, TURN_LENGTH);
		await("isStepFinished");
	}

	@GUARD
	private boolean isStepFinished() {
		return stepFinished;
	}

	@Override
	protected void updateRank() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void updateCurrency() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void saveState() {
		// TODO Auto-generated method stub
	}
}
