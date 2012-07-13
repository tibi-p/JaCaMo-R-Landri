/**
 * 
 */
package org.aria.rlandri.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Tiberiu Popa
 * 
 */
public class JaCaMoConfig {

	private static final String CONFIG_FILENAME = "config.properties";

	public static void writeProperties(String sandboxPath,
			Map<String, String> propMap) throws IOException {
		Properties properties = new Properties();
		for (Map.Entry<String, String> entry : propMap.entrySet())
			properties.setProperty(entry.getKey(), entry.getValue());
		File configFile = new File(sandboxPath, CONFIG_FILENAME);
		properties.store(new FileOutputStream(configFile),
				"Properties for sub-environment configuration");
	}

	/**
	 * @param args
	 *            tool arguments: the path to the JaCaMo sandbox
	 */
	public static void main(String[] args) {
		if (args.length >= 3) {
			String sandboxPath = args[0];
			Map<String, String> propMap = new HashMap<String, String>();
			String currentDirectory = System.getProperty("user.dir");
			propMap.put("django_directory", currentDirectory);
			propMap.put("environment_type", args[1]);
			propMap.put("coordinator_class", args[2]);
			try {
				writeProperties(sandboxPath, propMap);
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
