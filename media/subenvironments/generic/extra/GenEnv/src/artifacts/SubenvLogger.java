import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import cartago.Artifact;
import cartago.OPERATION;

import org.apache.log4j.*;

/**
 * @author Mihai
 *
 */
public class SubenvLogger extends Artifact {
	/*
	 * @cache: keep a mapping between agents and output
	 * 			streams towards a file so we don't reopen
	 * 			files every time we want to write the logs
	 */
	HashMap<String, Logger> loggers;
	ArrayList<String> override;
	String subEnvName;
	
	public void init(){
		loggers = new HashMap<String, Logger>();
		override = new ArrayList<String>();
		//find the mas2jFile to extract the subenvironment name
		String mas2jFile = new File(".").list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("mas2j");
			}
		})[0];
		subEnvName = mas2jFile.substring(mas2jFile.lastIndexOf('_') + 1, mas2jFile.length() - 6);
		
	}
	
	@OPERATION
	public void logInfo(String line){
		Logger l = fetchLogger(getOpUserName());
		l.info(line);
	}
	
	@OPERATION
	public void logWarning(String line){
		Logger l = fetchLogger(getOpUserName());
		l.warn(line);
	}
	
	@OPERATION
	public void logError(String line){
		Logger l = fetchLogger(getOpUserName());
		l.error(line);
	}
	
	@OPERATION
	public void logfatal(String line){
		Logger l = fetchLogger(getOpUserName());
		l.fatal(line);
	}
	
	
	
	private Logger fetchLogger(String agent) {
		/*
		 * @agent: the jason name of the agent
		 * @agentBaseName: the 'root' name of the agent
		 * 					e.g.: "Rambo4 -> 'Rambo'
		 * 					Used to identify clones
		 */
		String agentBaseName;
		
		if(Character.isDigit(agent.charAt(agent.length() - 1))){
			/*
			 * if agent's name ends in a number, remove that number
			 * to form the base name
			 */
			int index = agent.length() - 2;
			
			while(Character.isDigit(agent.charAt(index))){
				index--;
			}
			
			agentBaseName = agent.substring(0, index + 1);
		}
		else{
			agentBaseName = agent;
		}
		
		if(loggers.containsKey(agent)) return loggers.get(agent);
		else{
			File agDir = new File ("media/agents/" + agentBaseName);
			/*temporary trick to make the home dir in the tmp folders*/
			if(!agDir.exists()) agDir.mkdirs(); //make the agent's home directory
			
			Date date = Calendar.getInstance().getTime();
			DateFormat day = new SimpleDateFormat("dd-MM-yy");
			
			File agLog = new File(agDir.getAbsolutePath() + "/" + subEnvName + "_" + day.format(date) + ".log"); //the log for that day
			System.out.println(agLog.getAbsolutePath());
			if(!agLog.exists()){ //make sure file exists 
				try {
					agLog.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}
			else if(!override.contains(agentBaseName)){//truncate if necessary 
				try {
					FileOutputStream eraser = new FileOutputStream(agLog);
					eraser.close();
					override.add(agentBaseName);
				} catch (FileNotFoundException e) {
					//impossible for file to not exist
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			Logger logger = Logger.getLogger(agent);
			loggers.put(agent, logger);
			Layout layout = new EnhancedPatternLayout("[%d{HH:mm:ss}]["
														+ agent 
														+ "][%p]: %m%n");
			FileAppender appender;
			try {
				appender = new FileAppender(layout, agLog.getAbsolutePath(), true);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			logger.removeAllAppenders();
			logger.addAppender(appender);
			return logger;
		}
	}
}
