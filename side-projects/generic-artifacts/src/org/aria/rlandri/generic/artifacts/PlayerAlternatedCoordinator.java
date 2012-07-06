package org.aria.rlandri.generic.artifacts;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cartago.AgentId;
import cartago.ArtifactOpMethod;
import cartago.CartagoException;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

/**
 * @author Andrei Geacar
 *
 */

public class PlayerAlternatedCoordinator extends Coordinator {

	int currentStep = 0;
	int currentAgent = 0;
	
	HashMap<String,AgentId> agents;
	List<String> order;
	
	ArtifactOpMethod turnOp;
	
	@Override
	void startSubenv() throws CartagoException 
	{
		super.init();
		agents= new HashMap<String,AgentId>();
		order  = new LinkedList<String>();
	}

	@Override
	void finishSubenv()
	{
		
	}
	
	@OPERATION
	void registerAgent(OpFeedbackParam<String> wsp)
	{
		AgentId aid= this.getOpUserId();
		String name = this.getOpUserName();
		if(order.contains(name))
			return;
		agents.put(name,aid);
		order.add(name);
		wsp.set("NA");
	}
	
	


	private void startStep()
	{
		currentStep+=1;
		currentAgent=0;
		signal("startStep");
	}


	private void startPlayerTurn()
	{
		String name= order.get(currentAgent);
		AgentId aid = agents.get(name);
		signal(aid,"startTurn");
		currentAgent+=1;
		
	}
	

	private void  processTurn()
	{
		//turnOp.execSavedParameters();
		if(currentAgent==order.size())
		{
			currentAgent = 0;
		}
	}
	
	
	
	
}
