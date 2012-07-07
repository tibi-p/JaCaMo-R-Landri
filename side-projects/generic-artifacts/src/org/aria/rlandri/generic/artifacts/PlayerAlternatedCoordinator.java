package org.aria.rlandri.generic.artifacts;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cartago.AgentId;
import cartago.CartagoException;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

/**
 * @author Andrei Geacar
 *
 */

public class PlayerAlternatedCoordinator extends Coordinator {

	private int currentStep = 0;
	private int currentAgent = 0;

	HashMap<String,AgentId> agents;
	List<String> order;

	GameArtifactOpMethod turnOp;

	// constants for testing purposes
	public static final int STEPS = 10;
	public static final int TURN_LENGTH = 1000;

	@Override
	void startSubenv() throws CartagoException
	{
		super.init();
		agents= new HashMap<String,AgentId>();
		order  = new LinkedList<String>();

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

		// Subenv starts running when every participant registered
		if(participants.size()==order.size())
		{
			runSubEnv();
		}

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
			turnOp.execSavedParameters();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(currentAgent==order.size())
		{
			currentAgent = 0;
		}
	}
	// TODO internal action to run subenv

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
