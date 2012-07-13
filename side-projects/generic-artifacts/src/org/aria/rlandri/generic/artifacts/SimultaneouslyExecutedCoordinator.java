package org.aria.rlandri.generic.artifacts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.collections.map.MultiValueMap;
import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.opmethod.PrimeAgentArtifactOpMethod;
import org.aria.rlandri.generic.artifacts.opmethod.SETBGameArtifactOpMethod;

import cartago.AgentId;
import cartago.CartagoException;
import cartago.GUARD;
import cartago.IArtifactOp;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class SimultaneouslyExecutedCoordinator extends Coordinator {

	private final Timer timer = new Timer();
	private int currentStep = 0;

	public static final int STEPS = 10;
	public static final int STEP_LENGTH = 1000;

	private MultiValueMap operationQueue = new MultiValueMap();
	private List<String> agentOrder = new ArrayList<String>();
	private int numReadyAgents = 0;
	private int executingAgentIndex = 0;

	public void addOpMethod(IArtifactOp op, Object[] params) {
		AgentId agentId = getOpUserId();
		operationQueue.put(agentId, new ParameterizedOperation(op, params));
	}

	public boolean waitForEndTurn() {
		numReadyAgents++;
		System.err.println(String.format("%d agents are ready from %d",
				numReadyAgents, agents.size()));
		agentOrder.add(getOpUserName());
		if (!isEverybodyReady()) {
			await("isEverybodyReady");
		} else {
			setState(EnvStatus.EVALUATING);
		}
		await("isItMyTurn");
		if (executingAgentIndex == numReadyAgents) {
			resetTurnInfo();
			return true;
		} else {
			return false;
		}
	}

	private void resetTurnInfo() {
		agentOrder.clear();
		numReadyAgents = 0;
		executingAgentIndex = 0;
	}

	@OPERATION
	void registerAgent() {

	}

	@OPERATION
	void runQueuedOperations() {
		System.out.println("SPARTAAAAAA!");
		for (Object key : operationQueue.keySet()) {
			Collection<?> coll = operationQueue.getCollection(key);
			for (Object value : coll) {
				if (value instanceof ParameterizedOperation) {
					ParameterizedOperation entry = (ParameterizedOperation) value;
					try {
						SETBGameArtifactOpMethod op = (SETBGameArtifactOpMethod) entry
								.getOp();
						op.execSavedParameters(entry.getParams());
					} catch (Exception e) {
						// TODO log it or something
						e.printStackTrace();
					}
				}
			}
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
	}

	private void executeStep() {
		timer.schedule(new TimerTask() {
			public void run() {
				execInternalOp("changeToEvaluating");
			}
		}, STEP_LENGTH);
		await("isNotRunning");
	}

	@INTERNAL_OPERATION
	void changeToEvaluating() {
		EnvStatus state = getState();
		if (state == EnvStatus.RUNNING) {
			setState(EnvStatus.EVALUATING);
		} else {
			// TODO handle me
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
	void registerAgent(OpFeedbackParam<String> wsp) throws Exception {
		super.registerAgent(wsp);
		wsp.set("NA");
	}

	@GUARD
	boolean isEverybodyReady() {
		return numReadyAgents == agents.size();
	}

	@GUARD
	boolean isItMyTurn() {
		String currentAgent = agentOrder.get(executingAgentIndex);
		if (currentAgent.equals(getOpUserName())) {
			executingAgentIndex++;
			return true;
		} else {
			return false;
		}
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
