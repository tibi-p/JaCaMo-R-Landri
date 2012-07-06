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

import cartago.Artifact;
import cartago.CartagoException;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public abstract class Coordinator extends Artifact {
	// TODO transform to map: String -> (AgentId, status)
	ArrayList<String> participants;
	enum EnvStatus { PRIMORDIAL, INITIATED, RUNNING, EVALUATING, FINISHED};
	EnvStatus state = EnvStatus.PRIMORDIAL;
	public static final int realTimeSP = 0, realTimeNeg = 1, turnBasedSimultaneous = 2, turnBasedAlternative = 3;
	
	
	void init() throws CartagoException {
		try{
			participants = new ArrayList<String>();
			
			File mas2jFile = new File(".").listFiles(new FileFilter() {
				
				public boolean accept(File arg0) {
					return arg0.getAbsolutePath().endsWith("mas2j");
				}
			})[0];
			
			mas2j parser = new mas2j(new FileInputStream(mas2jFile));
			MAS2JProject project = parser.mas();
			for(AgentParameters ap : project.getAgents()){
				if(!ap.getAgName().contains("prime_agent_s"))
					participants.add(ap.getAgName());
			}
			state = EnvStatus.INITIATED;
		} catch (FileNotFoundException e) {
			System.err.println("Could not find mas2j file");
		} catch (ParseException e){
			System.err.println("Parse exception for mas2j file");
		}
	}

	@OPERATION
	abstract void registerAgent(OpFeedbackParam<String> wsp);
	
	@OPERATION
	abstract void startSubenv();
	
	@OPERATION
	abstract void finishSubenv();
}
