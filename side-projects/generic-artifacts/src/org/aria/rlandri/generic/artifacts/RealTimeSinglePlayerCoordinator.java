package org.aria.rlandri.generic.artifacts;

import java.util.HashMap;

import cartago.AgentId;
import cartago.ArtifactOpMethod;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public abstract class RealTimeSinglePlayerCoordinator extends Coordinator {
	HashMap<AgentId, ArtifactOpMethod> enqueuedOps;
	private int index;
	public void init(){
		super.init();
		index = 0;
		enqueuedOps = new HashMap<AgentId, ArtifactOpMethod>();
	}
	@OPERATION
	private void registerAgent(OpFeedbackParam<String> privateSubenv){
	    
	}
	
	@OPERATION
	private void getNextAgent(OpFeedbackParam<String> agentName){
		if(index == participants.size()) agentName.set("no_more");
		else agentName.set(participants.get(index));
	}
	
	abstract void registerOperations();
	
	@OPERATION
	void checkStatus(OpFeedbackParam<String> status){}
	
	@OPERATION
	private void finishEnv(){
		updateRank();
		updateCurrency();
		saveState();
	}
	
	abstract void updateRank();
	abstract void updateCurrency();
	abstract void saveState();
	
	@OPERATION
	void startSubenv() {
		if (getOpUserName().equals("prime_agent_s_generic")){
			signal("startSubenv");
			state = EnvStatus.RUNNING;
		}
	}
}
