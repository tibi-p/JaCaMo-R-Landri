package org.aria.rlandri.generic.artifacts;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.opmethod.PATBGameArtifactOpMethod;

import cartago.AgentId;
import cartago.CartagoException;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

/**
 * @author Andrei Geacar
 * 
 */

public class PlayerAlternatedCoordinator extends Coordinator {

	private int currentStep = 0;
	private int currentAgent = 0;

	List<String> order = new LinkedList<String>();

	// constants for testing purposes
	public static final int STEPS = 10;
	public static final int TURN_LENGTH = 1000;

	// TODO use status here
	@OPERATION
	void registerAgent(OpFeedbackParam<String> wsp) throws Exception {
		super.registerAgent(wsp);
		String name = this.getOpUserName();
		order.add(name);
		wsp.set("NA");

	}

	public void failIfNotCurrentTurn() {
		String userName = getOpUserName();
		if (userName != order.get(currentAgent)) {
			// TODO: Standard error messages
			failed("Error message");
		}
	}

	@PRIME_AGENT_OPERATION
	void startSubenv() {
		super.startSubenv();
		execInternalOp("runSubEnv");
	}

	@Override
	protected void fillOperations() throws CartagoException {
		addOperation(new CoordinatorAnnotation(GAME_OPERATION.class,
				PATBGameArtifactOpMethod.class, true));
		addOperation(new CoordinatorAnnotation(PRIME_AGENT_OPERATION.class,
				PrimeAgentArtifactOpMethod.class, false));
	}

	@INTERNAL_OPERATION
	void runSubEnv() {
		for (currentStep = 1; currentStep <= STEPS; currentStep++) {
			executeStep();
		}
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

	private void executeStep() {
		for (currentAgent = 0; currentAgent < order.size(); currentAgent++) {
			startPlayerTurn();
			executePlayerTurn();
		}
	}

	private void startPlayerTurn() {
		String name = order.get(currentAgent);
		AgentId aid = agents.get(name);
		signal(aid, "startTurn", currentStep);
		currentAgent += 1;
	}

	private void executePlayerTurn() {
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				execInternalOp("changeToEvaluating");
			}
		}, TURN_LENGTH);
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

	// TODO remove me
	@GAME_OPERATION(validator = "catzelushCuParuCretz")
	void hotelCismigiu() {
		System.out.println("SA MA MUT IN HOTEL CISMIGIU");
	}

	// TODO remove me
	void catzelushCuParuCretz() {
		System.out.println("Toni da cu Grebla");
	}

}
