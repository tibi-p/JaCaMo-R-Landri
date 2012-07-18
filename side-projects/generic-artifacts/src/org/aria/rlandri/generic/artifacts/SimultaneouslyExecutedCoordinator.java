package org.aria.rlandri.generic.artifacts;

import java.util.ArrayList;
import java.util.Date;
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

	public static final int STEPS = 3;
	public static final int STEP_LENGTH = 1000;

	protected int currentStep = 0;

	private final Timer timer = new Timer();
	private TimerTask task = null;

	private final List<String> agentOrder = new ArrayList<String>();
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
		if (executingAgentIndex == agentOrder.size()) {
			resetTurnInfo();
			System.err.println(String.format("%s: IESII PI PRIMA CRACA",
					getOpUserId()));
			return true;
		} else {
			System.err.println(String.format("%s: IESII PI A DOUA CRACA",
					getOpUserId()));
			return false;
		}
	}

	private boolean isEverybodyReady() {
		return agentOrder.size() == regularAgents.getNumRegistered();
	}

	private void leaveNoAgentBehind() {
		agentOrder.add(getOpUserName());
		if (isEverybodyReady()) {
			System.err.println(String.format(
					"Submitting is over!!! remaining(%s)",
					task.scheduledExecutionTime() - new Date().getTime()));
			boolean canceled = task.cancel();
			System.err.println("Canceled: " + canceled);
			execInternalOp("transitionToEvaluating");
		} else {
			await("isSubmittingOver");
		}
	}

	private void resetTurnInfo() {
		agentOrder.clear();
		executingAgentIndex = 0;
		stepFinished = true;
		timerExpired = false;
	}

	@PRIME_AGENT_OPERATION
	void startSubenv() {
		super.startSubenv();
		execInternalOp("runSubEnv");
	}

	private void executeStep() {
		stepFinished = false;
		final long startTime = new Date().getTime();
		System.err.println("The task was scheduled at " + startTime);
		task = new TimerTask() {
			public void run() {
				String errFmt = "The task scheduled at %s was run at %s (diff=%s)";
				long runTime = new Date().getTime();
				System.err.println(String.format(errFmt, startTime, runTime,
						runTime - startTime));
				execInternalOp("finishTimer");
			}
		};
		signal("startTurn", currentStep);
		timer.schedule(task, STEP_LENGTH);
		await("isStepFinished");
		// TODO send only to master
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
	private void finishTimer() {
		if (!isEverybodyReady()) {
			EnvStatus state = getState();
			if (state == EnvStatus.RUNNING) {
				System.err.println("OOOOOOOOOO NU CE MORTZII MA-SII: "
						+ agentOrder.size() + " from "
						+ regularAgents.getNumRegistered());
				timerExpired = true;
				transitionToEvaluating();
			} else {
				// TODO handle me
			}
		}
	}

	@INTERNAL_OPERATION
	private void transitionToEvaluating() {
		System.err.println("OOOOOOOOOO NU CE MORTZII LU' TA-SU");
		setState(EnvStatus.EVALUATING);
	}

	@INTERNAL_OPERATION
	void runSubEnv() {
		for (currentStep = 1; currentStep <= STEPS; currentStep++)
			executeStep();
		for (AgentId agentId : primeAgents.getAgentIds())
			signal(agentId, "stopGame");
	}

	@GUARD
	private boolean isItMyTurn(AgentId agentId) {
		String currentAgent = agentOrder.get(executingAgentIndex);
		System.err.println(String.format(
				"TURN CHECKING: %s told %s to sod off", currentAgent, agentId));
		if (currentAgent.equals(agentId.getAgentName())) {
			executingAgentIndex++;
			return true;
		} else {
			return false;
		}
	}

	@GUARD
	private boolean isStepFinished() {
		return stepFinished;
	}

	@GUARD
	private boolean isSubmittingOver() {
		System.err.println(String.format(
				"Checking if submitting is over: answer (%s | %s) = %s",
				timerExpired, isEverybodyReady(), timerExpired
						|| isEverybodyReady()));
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
