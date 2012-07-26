package org.aria.rlandri.generic.artifacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.opmethod.PATBGameArtifactOpMethod;
import org.aria.rlandri.generic.artifacts.opmethod.PrimeAgentArtifactOpMethod;
import org.aria.rlandri.generic.artifacts.tools.ValidationType;

import cartago.AgentId;
import cartago.CartagoException;
import cartago.GUARD;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

/**
 * The abstract coordinator class for player-alternated turn-based
 * sub-environments.
 * 
 * @author Andrei Geacar
 */
public abstract class PlayerAlternatedCoordinator extends Coordinator {

	protected AgentId currentAgent = null;
	protected int currentIndex = 0;

	protected final List<AgentId> order = new ArrayList<AgentId>();
	private final Timer timer = new Timer();
	private TimerTask task = null;
	private int currentStep = 0;
	private boolean stepFinished = true;

	// constants for testing purposes
	protected int steps;
	protected static final int TURN_LENGTH = 1000;

	@Override
	protected void init() throws CartagoException
	{
		super.init();
		this.steps = Integer.parseInt(prop.getProperty("num_steps"));
	}
	
	public void prepareEvaluation() {
		boolean cancelled = task.cancel();
		System.err.println(String.format("Cancellation has%s succeeded",
				cancelled ? "" : " not"));
		setState(EnvStatus.EVALUATING);
	}

	public void resetTurnInfo() {
		stepFinished = true;
	}

	public void failIfNotCurrentTurn() {
		AgentId userId = getOpUserId();
		if (!userId.equals(currentAgent))
			failTurn("not_your_turn", ValidationType.ERROR);
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
	protected void registerAgent(OpFeedbackParam<String> wsp) {
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
		for (currentStep = 1; currentStep <= steps; currentStep++) {
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
}
