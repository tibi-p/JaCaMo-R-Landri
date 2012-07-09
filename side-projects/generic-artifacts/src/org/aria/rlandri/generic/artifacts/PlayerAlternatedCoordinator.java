package org.aria.rlandri.generic.artifacts;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotation;
import org.aria.rlandri.generic.artifacts.annotation.GuardedAnnotationProcessor;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;

import cartago.AgentId;
import cartago.CartagoException;
import cartago.IArtifactOp;
import cartago.OPERATION;

/**
 * @author Andrei Geacar
 *
 */

public class PlayerAlternatedCoordinator extends Coordinator {

	private int currentStep = 0;
	private int currentAgent = 0;

	
	List<String> order = new LinkedList<String>();

	ParameterizedOperation turnOpClosure;

	// constants for testing purposes
	public static final int STEPS = 10;
	public static final int TURN_LENGTH = 1000;

	/*

	// TODO use status here
		@OPERATION
		void registerAgent(OpFeedbackParam<String> wsp) throws InterruptedException
		{
			AgentId aid= this.getOpUserId();
			String name = this.getOpUserName();
			if(order.contains(name))
				return;
			agents.put(name,aid);
			order.add(name);
			wsp.set("NA");

		}
	
	*/
	
	public void addOpMethod(IArtifactOp op, Object[] params) {
		AgentId agentId = getOpUserId();
		turnOpClosure = new ParameterizedOperation(op, params);
	}
	
	@OPERATION
	void startSubenv() throws InterruptedException{
		super.startSubenv();
		runSubEnv();
	}

	@Override
	protected void registerCustomOperations() throws CartagoException {
		List<GuardedAnnotation> annotations = new ArrayList<GuardedAnnotation>();
		annotations.add(new GuardedAnnotation(GAME_OPERATION.class,
				SETBGameArtifactOpMethod.class) {

			@Override
			public void processMethod(Method method) throws CartagoException {
				addCustomOperation(this, method);
			}

		});
		annotations.add(new GuardedAnnotation(PRIME_AGENT_OPERATION.class,
				PrimeAgentArtifactOpMethod.class) {

			@Override
			public void processMethod(Method method) throws CartagoException {
				addCustomOperation(this, method);
			}

		});

		GuardedAnnotationProcessor processor = new GuardedAnnotationProcessor(
				getClass());
		processor.processAnnotations(annotations);
	}

	private void runSubEnv() throws InterruptedException
	{
		for(currentStep = 1;currentStep<=STEPS;currentStep++)
		{
			executeStep();
		}
	}

	@Override
	void finishSubenv()
	{
		
	}

	
	private void executeStep() throws InterruptedException
	{
		for(currentAgent=0;currentAgent<order.size();currentAgent++)
		{
			startPlayerTurn();
			wait(TURN_LENGTH);
			processTurn();
		}
	}

	private void startPlayerTurn()
	{
		String name= order.get(currentAgent);
		AgentId aid = agents.get(name);
		signal(aid,"startTurn",currentStep);
		currentAgent+=1;
	}

	private void  processTurn()
	{
		try {
			PATBGameArtifactOpMethod op = (PATBGameArtifactOpMethod)turnOpClosure.getOp();
			op.execSavedParameters(turnOpClosure.getParams());
		} catch (Exception e) {
			// TODO log it or something
			e.printStackTrace();
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
