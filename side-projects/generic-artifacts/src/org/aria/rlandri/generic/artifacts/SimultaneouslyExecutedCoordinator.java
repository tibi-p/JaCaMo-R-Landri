package org.aria.rlandri.generic.artifacts;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
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

	public static final int STEPS = 4;
	public static final int STEP_LENGTH = 1000;

	protected int currentStep = 0;

	private final Timer timer = new Timer();
	private TimerTask task = null;

	private final Set<AgentId> readyAgents = new LinkedHashSet<AgentId>();
	private Iterator<AgentId> executingAgentIterator = null;
	private AgentId executingAgent = null;
	private boolean stepFinished = true;
	private boolean timerExpired = false;

	public boolean waitForEndTurn() {
		System.err.println(String.format("%s: waiting for the end of turn %s",
				getOpUserId(), currentStep));
		leaveNoAgentBehind();
		System.err.println(String.format(
				"%s: every agent has submitted its action", getOpUserId()));
		await("checkTurn", getOpUserId());
		System.err.println(String.format("%s: kill the bugs", getOpUserId()));
		if (executingAgentIterator.hasNext()) {
			executingAgent = executingAgentIterator.next();
			return false;
		} else {
			return true;
		}
	}

	public void resetTurnInfo() {
		readyAgents.clear();
		executingAgentIterator = null;
		executingAgent = null;
		stepFinished = true;
		timerExpired = false;
	}

	/**
	 * Fails if the current agent has already submitted a move this turn.
	 */
	public void failIfHasMoved() {
		if (hasMoved(getOpUserId()))
			failed("The current agent has already submitted a move this turn");
	}

	private boolean hasMoved(AgentId agentId) {
		return readyAgents.contains(agentId);
	}

	private boolean isEverybodyReady() {
		return readyAgents.size() == regularAgents.getNumRegistered();
	}

	private void leaveNoAgentBehind() {
		readyAgents.add(getOpUserId());
		if (isEverybodyReady()) {
			System.err.println(String.format(
					"Submitting is over!!! remaining(%s)",
					task.scheduledExecutionTime() - new Date().getTime()));
			boolean cancelled = task.cancel();
			System.err.println(String.format("Cancellation has%s succeeded",
					cancelled ? "" : " not"));
			if (prepareEvaluation()) {
				// TODO this can be skipped if it's this agent's turn!
				execInternalOp("wakerOfAgents");
			} else {
				// TODO document/log this impossible situation
				System.exit(1);
			}
		} else {
			await("isSubmittingOver");
		}
	}

	private boolean prepareEvaluation() {
		setState(EnvStatus.EVALUATING);
		executingAgentIterator = readyAgents.iterator();
		if (executingAgentIterator.hasNext()) {
			executingAgent = executingAgentIterator.next();
			return true;
		} else {
			System.err.println("WOP WOP WOP WOP");
			resetTurnInfo();
			return false;
		}
	}

	@PRIME_AGENT_OPERATION
	void startSubenv() {
		super.startSubenv();
		execInternalOp("runSubEnv");
	}

	private void executeStep() {
		setState(EnvStatus.RUNNING);
		stepFinished = false;
		final long startTime = new Date().getTime();
		System.err.println("PRIME: The task was scheduled at " + startTime);
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
		EnvStatus state = getState();
		if (state == EnvStatus.RUNNING) {
			String errFmt = "As the timer expired - %s from %s";
			System.err.println(String.format(errFmt, readyAgents.size(),
					regularAgents.getNumRegistered()));
			timerExpired = true;
			prepareEvaluation();
		} else {
			// TODO handle me
		}
	}

	@INTERNAL_OPERATION
	private void wakerOfAgents() {
		System.err.println("DID YOU PAY THE IRON PRICE FOR IT OR THE GOLD?");
	}

	@INTERNAL_OPERATION
	void runSubEnv() {
		for (currentStep = 1; currentStep <= STEPS; currentStep++)
			executeStep();
		for (AgentId agentId : primeAgents.getAgentIds())
			signal(agentId, "stopGame");
	}

	@GUARD
	private boolean checkTurn(AgentId agentId) {
		if (agentId != null)
			return agentId.equals(executingAgent);
		else
			return false;
	}

	@GUARD
	private boolean isStepFinished() {
		return stepFinished;
	}

	@GUARD
	private boolean isSubmittingOver() {
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
