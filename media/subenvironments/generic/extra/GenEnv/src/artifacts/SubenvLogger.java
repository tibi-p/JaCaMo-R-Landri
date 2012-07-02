import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import cartago.Artifact;
import cartago.OPERATION;

import org.apache.log4j.*;

/**
 * @author Mihai
 *
 */
public class SubenvLogger extends Artifact {
	/*
	 * @cache: Keep a mapping between agents and output
	 * 			streams towards a file so we don't reopen
	 * 			files every time we want to write the logs
	 * 
	 * @subEnvName: The name of the subenvironment that
	 * 				will be logged
	 * 
	 * @homeFolder: The folder where the logs will be put.
	 */
	HashMap<String, Logger> loggers;
	ArrayList<String> override;
	String subEnvName, homeFolder;
	
	public void init(){
		Properties prop = new Properties();
		
		try {
			prop.load(new FileInputStream("config.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		homeFolder = prop.getProperty("django_directory");
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
		int pos = agent.lastIndexOf('_');
		if (pos < 1) {
			String msg = "The agent name does not respect the required format";
			throw new IllegalArgumentException(msg);
		}

		String agentBaseName = agent.substring(0, pos);
		// Remove final underscore if cardinality is 1
		if (pos == agent.length() - 1)
			agent = agentBaseName;

		if (loggers.containsKey(agent)) {
			return loggers.get(agent);
		}
		else{
			File agDir = new File (homeFolder + "/" + agentBaseName);
			if(!agDir.exists()) agDir.mkdir(); //make the agent's home directory if it doesn't exist
			
			Date date = Calendar.getInstance().getTime();
			DateFormat day = new SimpleDateFormat("dd-MM-yy");
			
			File agLog = new File(agDir.getAbsolutePath() + "/" + subEnvName + "_" + day.format(date) + ".log"); //the log for that day
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
