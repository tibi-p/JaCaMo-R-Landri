package org.aria.rlandri.generic.artifacts;

import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.mas2j.parser.mas2j;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import cartago.AgentId;
import cartago.Artifact;
import cartago.CartagoException;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public abstract class Coordinator extends Artifact {
	HashMap<String,AgentId> agents;
	enum EnvStatus { PRIMORDIAL, INITIATED, RUNNING, EVALUATING, FINISHED};
	EnvStatus state = EnvStatus.PRIMORDIAL;
	public static final int realTimeSP = 0, realTimeNeg = 1, turnBasedSimultaneous = 2, turnBasedAlternative = 3;

	void init() throws CartagoException {
		try{
			agents = new HashMap<String, AgentId>();
			
			File mas2jFile = new File(".").listFiles(new FileFilter() {
				
				public boolean accept(File arg0) {
					return arg0.getAbsolutePath().endsWith("mas2j");
				}
			})[0];
			
			mas2j parser = new mas2j(new FileInputStream(mas2jFile));
			MAS2JProject project = parser.mas();
			for(AgentParameters ap : project.getAgents()){
				if(!ap.getAgName().startsWith("prime_agent_s_"))
					if(ap.qty == 1){
						agents.put(ap.getAgName(), null);
					}
					else for(int i = 1; i <= ap.qty; i++){
						agents.put(ap.getAgName() + "_" + i, null);
					}
			}
			state = EnvStatus.INITIATED;
		} catch (FileNotFoundException e) {
			System.err.println("Could not find mas2j file");
		} catch (ParseException e){
			System.err.println("Parse exception for mas2j file");
		}
	}

	protected abstract void updateRank();

	protected abstract void updateCurrency();

	protected abstract void saveState();

	@OPERATION
	abstract void registerAgent(OpFeedbackParam<String> wsp) throws Exception;
	
	@OPERATION
	abstract void startSubenv() throws Exception;
	
	@OPERATION
	abstract void finishSubenv();

}
