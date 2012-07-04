/**
 * 
 */
package org.aria.rlandri.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Tiberiu Popa
 * 
 */
public class JaCaMoConfig {

	public static void writeProperties(String sandboxPath) throws IOException {
		Properties properties = new Properties();
		String currentDirectory = System.getProperty("user.dir");
		properties.setProperty("django_directory", currentDirectory);
		File configFile = new File(sandboxPath, "config.properties");
		properties.store(new FileOutputStream(configFile),
				"Properties for sub-environment configuration");
	}

	/**
	 * @param args
	 *            tool arguments: the path to the JaCaMo sandbox
	 */
	public static void main(String[] args) {
		if (args.length >= 1) {
			String sandboxPath = args[0];
			try {
				writeProperties(sandboxPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			String errFmt = "%s: must be run like this '%s sandboxPath'";
			String className = JaCaMoConfig.class.getCanonicalName();
			System.err.println(String.format(errFmt, className, className));
		}
	}

}
