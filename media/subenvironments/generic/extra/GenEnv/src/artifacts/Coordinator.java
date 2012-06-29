import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.mas2j;
import cartago.*;
import jason.mas2j.parser.ParseException;

public class Coordinator extends Artifact {
	ArrayList<String> participants;
	
	void init() {
		/*try {
			participants = new ArrayList<String>();
			mas2j parser = new mas2j(new FileInputStream("derp.mas2j"));
			MAS2JProject project = parser.mas();
			for(AgentParameters ap : project.getAgents()){
				participants.add(ap.getAgName());
			}
		} catch (FileNotFoundException e) {
			System.err.println("Could not find mas2j file");
		} catch (ParseException e){
			System.err.println("Parse exception for mas2j file");
		}*/
	}
	
	@OPERATION
	void startSubenv(){
		if(getOpUserName().equals("prime_agent")) signal("startSubenv");
	}
}

