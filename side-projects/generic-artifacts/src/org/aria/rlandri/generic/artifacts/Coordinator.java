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
import cartago.ArtifactConfig;
import cartago.OPERATION;
import cartago.OperationException;

public abstract class Coordinator extends Artifact {
	ArrayList<String> participants;
	enum EnvStatus { PRIMORDIAL,INITIATED,RUNNING,EVALUATING,FINISHED};
	EnvStatus state = EnvStatus.PRIMORDIAL;
	public static final int realTimeSP = 0, realTimeNeg = 1, turnBasedSimultaneous = 2, turnBasedAlternative = 3;
	
	
	void init() {
		try{
			participants = new ArrayList<String>();
			
			File mas2jFile = new File(".").listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File arg0) {
					return arg0.getAbsolutePath().endsWith("mas2j");
				}
			})[0];
			
			mas2j parser = new mas2j(new FileInputStream(mas2jFile));
			MAS2JProject project = parser.mas();
			for(AgentParameters ap : project.getAgents()){
				participants.add(ap.getAgName());
			}
			int subenvType = getSubenvType();
			switch(subenvType){
				case realTimeSP:
					setupRTSP();
					break;
			}
			state = EnvStatus.INITIATED;
		} catch (FileNotFoundException e) {
			System.err.println("Could not find mas2j file");
		} catch (ParseException e){
			System.err.println("Parse exception for mas2j file");
		}
	}

	private void setupRTSP() {
		try {
			makeArtifact("coordinator", "RealTimeSinglePlayerCoordinator", ArtifactConfig.DEFAULT_CONFIG);
		} catch (OperationException e) {
			e.printStackTrace();
		}
	}
	
	private int getSubenvType() {
		// TODO return a subenv type equal to one of the static integers
		return 0;
	}



	@OPERATION
	abstract void startSubenv();
	
	@OPERATION
	abstract void finishSubenv();

}
