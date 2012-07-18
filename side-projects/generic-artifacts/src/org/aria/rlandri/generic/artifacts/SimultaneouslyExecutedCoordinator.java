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
	private int currentStep = 0;

	public static final int STEPS = 3;
	public static final int STEP_LENGTH = 1000;

	private List<String> agentOrder = new ArrayList<String>();
	private int numReadyAgents = 0;
	private int executingAgentIndex = 0;
	private boolean stepFinished = true;
	private boolean executing = false;
	private boolean firstTimeOnBarrier = false;
	private boolean timeoutExpired = false;

	public void setExecuting(boolean isExecuting) {
		this.executing = isExecuting;
	}

	public boolean waitForEndTurn() {
		leaveNoAgentBehind();
		while (!isItMyTurn()) {
			// TODO use isItMyTurn w/ params = AgentId
			firstTimeOnBarrier = true;
			await("isNotExecuting");
		}
		setExecuting(true);
		if (executingAgentIndex == numReadyAgents) {
			resetTurnInfo();
			return true;
		} else {
			return false;
		}
	}

	private void leaveNoAgentBehind() {
		numReadyAgents++;
		agentOrder.add(getOpUserName());
		if (isEverybodyReady()) {
			setState(EnvStatus.EVALUATING);
		} else {
			await("isEverybodyReady");
		}
	}

	private void resetTurnInfo() {
		agentOrder.clear();
		numReadyAgents = 0;
		executingAgentIndex = 0;
		stepFinished = true;
	}

	private boolean isItMyTurn() {
		return isItMyTurn(getOpUserId());
	}

	private boolean isItMyTurn(AgentId agentId) {
		String currentAgent = agentOrder.get(executingAgentIndex);
		if (currentAgent.equals(agentId.getAgentName())) {
			executingAgentIndex++;
			return true;
		} else {
			return false;
		}
	}

	// TODO remove me
	@GAME_OPERATION(validator = "catzelushCuParuCretz")
	void hotelCismigiu() {
		System.out.println("SA MA MUT IN HOTEL CISMIGIU");
	}

	// TODO remove me
	void catzelushCuParuCretz() {
		System.out.println("Toni da cu Grebla");
	}

	@PRIME_AGENT_OPERATION
	void startSubenv() {
		super.startSubenv();
		execInternalOp("runSubEnv");
	}

	@INTERNAL_OPERATION
	void runSubEnv() {
		for (currentStep = 1; currentStep <= STEPS; currentStep++) {
			executeStep();
		}
		for (AgentId agentId : primeAgents.getAgentIds())
			signal(agentId, "stopGame");
	}

	private void executeStep() {
		stepFinished = false;
		signal("startTurn", currentStep);
		timer.schedule(new TimerTask() {
			public void run() {
				execInternalOp("changeToEvaluating");
			}
		}, STEP_LENGTH / 5);
		await("isStepFinished");
		signal("stopTurn", currentStep);
	}

	@INTERNAL_OPERATION
	void changeToEvaluating() {
		if (!isEverybodyReady()) {
			EnvStatus state = getState();
			if (state == EnvStatus.RUNNING) {
				System.err.println("OOOOOOOOOO NU CE MORTZII MA-SII");
				timeoutExpired = true;
				setState(EnvStatus.EVALUATING);
			} else {
				// TODO handle me
			}
		}
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

	@GUARD
	boolean isEverybodyReady() {
		if (timeoutExpired)
			return true;
		else
			return numReadyAgents == regularAgents.getNumRegistered();
	}

	@GUARD
	private boolean isNotExecuting() {
		if (firstTimeOnBarrier) {
			firstTimeOnBarrier = false;
			return false;
		} else {
			return !executing;
		}
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
