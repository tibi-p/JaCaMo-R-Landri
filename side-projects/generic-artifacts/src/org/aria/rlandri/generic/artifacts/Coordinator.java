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
import cartago.OPERATION;

public class Coordinator extends Artifact {
	ArrayList<String> participants;
	boolean initiated = false, running = false, finished = false;
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
			initiated = true;
		} catch (FileNotFoundException e) {
			System.err.println("Could not find mas2j file");
		} catch (ParseException e){
			System.err.println("Parse exception for mas2j file");
		}
	}

	@OPERATION
	void startSubenv() {
		if (getOpUserName().equals("prime_agent_s_generic")){
			signal("startSubenv");
			running = true;
		}
	}

}
