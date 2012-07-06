package org.aria.rlandri.generic.artifacts;

import java.util.HashMap;

import cartago.AgentId;
import cartago.ArtifactOpMethod;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

abstract public class RealTimeSinglePlayerCoordinator extends Coordinator {
	HashMap<AgentId, ArtifactOpMethod> enqueuedOps;
	private int index;
	public void init(){
		super.init();
		index = 0;
		enqueuedOps = new HashMap<AgentId, ArtifactOpMethod>();
	}
	@OPERATION
	void registerAgent(OpFeedbackParam<String> privateSubenv){
	    privateSubenv.set(getOpUserName() + "_private_workspace");
	}
	
	@OPERATION
	private void getNextAgent(OpFeedbackParam<String> wspName){
		if(index == participants.size()) wspName.set("no_more");
		else wspName.set(participants.get(index) + "_private_workspace");
	}
	
	@OPERATION
	private void incrementIndex(){
		index++;
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
	//////////////////////////////////
	abstract void updateRank();
	abstract void updateCurrency();
	abstract void saveState();
	
	@OPERATION
	void startSubenv() {
		if (getOpUserName().equals("prime_agent_s_rtsp")){
			signal("startSubenv");
			state = EnvStatus.RUNNING;
		}
	}
}
