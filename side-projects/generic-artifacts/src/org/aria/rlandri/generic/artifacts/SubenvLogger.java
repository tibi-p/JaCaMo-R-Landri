package org.aria.rlandri.generic.artifacts;

import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.mas2j.parser.mas2j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;

import cartago.Artifact;
import cartago.CartagoException;
import cartago.OPERATION;

/**
 * @author Mihai Poenaru
 * 
 */
public class SubenvLogger extends Artifact {

	public static final String LOG_REL_PATH = "media/agents";
	private static final DateFormat day = new SimpleDateFormat("dd-MM-yy");

	/**
	 * The name of the subenvironment that will be logged.
	 */
	private String subEnvName;
	/**
	 * The folder where the logs will be put.
	 */
	private File logDirectory = new File(".");

	public void init() throws CartagoException {
		Configuration configuration = Configuration.getInstance();
		String djangoDirectory = configuration.getProperty("django_directory");
		logDirectory = new File(djangoDirectory, LOG_REL_PATH);

		// find the mas2jFile to extract the subenvironment name
		String mas2jFile = new File(".").list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("mas2j");
			}
		})[0];
		subEnvName = mas2jFile.substring(mas2jFile.lastIndexOf('_') + 1,
				mas2jFile.length() - 6);

		try {
			initLoggers(mas2jFile);
		} catch (ParseException e) {
			throw new CartagoException(e.getMessage());
		} catch (IOException e) {
			throw new CartagoException(e.getMessage());
		}
	}

	@OPERATION
	public void logInfo(Object... strings) {
		Logger l = Logger.getLogger(getOpUserName());
		l.info(StringUtils.join(strings));
	}

	@OPERATION
	public void logWarning(Object... strings) {
		Logger l = Logger.getLogger(getOpUserName());
		l.warn(StringUtils.join(strings));
	}

	@OPERATION
	public void logError(Object... strings) {
		Logger l = Logger.getLogger(getOpUserName());
		l.error(StringUtils.join(strings));
	}

	@OPERATION
	public void logFatal(Object... strings) {
		Logger l = Logger.getLogger(getOpUserName());
		l.fatal(StringUtils.join(strings));
	}

	private void initLoggers(String filename) throws ParseException,
			IOException {
		mas2j parser = new mas2j(new FileInputStream(filename));
		MAS2JProject project = parser.mas();
		for (AgentParameters ap : project.getAgents()) {
			String agentName = ap.getAgName();
			createLogger(agentName, ap.qty);
		}
	}

	private void createLogger(String baseName, int qty) throws IOException {
		String date = day.format(Calendar.getInstance().getTime());

		// the log for the current day
		String filename = String.format("%s_%s.log", subEnvName, date);
		File agDir = new File(logDirectory, baseName);
		File agLog = new File(agDir, filename);
		agLog.delete();

		String logPath = agLog.getAbsolutePath();
		if (qty > 1) {
			for (int i = 1; i <= qty; i++)
				addLoger(baseName + i, logPath);
		} else {
			addLoger(baseName, logPath);
		}
	}

	private void addLoger(String agentName, String logPath) throws IOException {
		Logger logger = Logger.getLogger(agentName);
		Layout layout = new EnhancedPatternLayout("[%d{HH:mm:ss}][" + agentName
				+ "][%p]: %m%n");
		FileAppender appender = new FileAppender(layout, logPath, true);
		logger.removeAllAppenders();
		logger.addAppender(appender);
	}

}
