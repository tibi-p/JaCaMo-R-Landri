package org.aria.rlandri.generic.artifacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.opmethod.PrimeAgentArtifactOpMethod;
import org.aria.rlandri.generic.artifacts.opmethod.SETBGameArtifactOpMethod;

import cartago.AgentId;
import cartago.CartagoException;
import cartago.GUARD;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class SimultaneouslyExecutedCoordinator extends Coordinator {

	private final Timer timer = new Timer();
	private TimerTask task = null;
	private int currentStep = 0;

	public static final int STEPS = 3;
	public static final int STEP_LENGTH = 1000;

	private final List<String> agentOrder = new ArrayList<String>();
	private int numReadyAgents = 0;
	private int executingAgentIndex = 0;
	private boolean stepFinished = true;
	private boolean timerExpired = false;

	public boolean waitForEndTurn() {
		System.err.println(String.format("%s: waiting for end turn",
				getOpUserId()));
		leaveNoAgentBehind();
		System.err.println(String.format(
				"%s: every agent has submitted its action", getOpUserId()));
		await("isItMyTurn", getOpUserId());
		System.err.println(String.format("%s: kill the bugs", getOpUserId()));
		if (executingAgentIndex == numReadyAgents) {
			resetTurnInfo();
			return true;
		} else {
			return false;
		}
	}

	private boolean isEverybodyReady() {
		return numReadyAgents == regularAgents.getNumRegistered();
	}

	private void leaveNoAgentBehind() {
		numReadyAgents++;
		agentOrder.add(getOpUserName());
		if (isSubmittingOver()) {
			setState(EnvStatus.EVALUATING);
		} else {
			await("isSubmittingOver");
		}
	}

	private void resetTurnInfo() {
		agentOrder.clear();
		numReadyAgents = 0;
		executingAgentIndex = 0;
		stepFinished = true;
	}

	@GUARD
	private boolean isItMyTurn(AgentId agentId) {
		String currentAgent = agentOrder.get(executingAgentIndex);
		if (currentAgent.equals(agentId.getAgentName())) {
			executingAgentIndex++;
			return true;
		} else {
			return false;
		}
	}

	@PRIME_AGENT_OPERATION
	void startSubenv() {
		super.startSubenv();
		execInternalOp("runSubEnv");
	}

	private void executeStep() {
		stepFinished = false;
		signal("startTurn", currentStep);
		task = new TimerTask() {
			public void run() {
				execInternalOp("changeToEvaluating");
			}
		};
		timer.schedule(task, STEP_LENGTH / 1);
		await("isStepFinished");
		signal("stopTurn", currentStep);
	}

	@Override
	protected void fillOperations() throws CartagoException {
		addOperation(new CoordinatorAnnotation(GAME_OPERATION.class,
				SETBGameArtifactOpMethod.class, true));
		addOperation(new CoordinatorAnnotation(PRIME_AGENT_OPERATION.class,
				PrimeAgentArtifactOpMethod.class, false));
	}

	@OPERATION
	void registerAgent(OpFeedbackParam<String> wsp) {
		super.registerAgent(wsp);
		wsp.set("NA");
	}

	@INTERNAL_OPERATION
	private void changeToEvaluating() {
		if (!isEverybodyReady()) {
			EnvStatus state = getState();
			if (state == EnvStatus.RUNNING) {
				System.err.println("OOOOOOOOOO NU CE MORTZII MA-SII");
				timerExpired = true;
				setState(EnvStatus.EVALUATING);
			} else {
				// TODO handle me
			}
		}
	}

	@INTERNAL_OPERATION
	void runSubEnv() {
		for (currentStep = 1; currentStep <= STEPS; currentStep++)
			executeStep();
		for (AgentId agentId : primeAgents.getAgentIds())
			signal(agentId, "stopGame");
	}

	@GUARD
	private boolean isStepFinished() {
		return stepFinished;
	}

	@GUARD
	boolean isSubmittingOver() {
		return timerExpired || isEverybodyReady();
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
