package org.aria.rlandri.generic.artifacts;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Configuration extends Properties {

	/**
	 * Generated serial version ID.
	 */
	private static final long serialVersionUID = -9056239039219813358L;

	private static final String CONFIG_FILENAME = "config.properties";

	private static final class ConfigurationSingleton {

		public static final Configuration INSTANCE = new Configuration();

	}

	private Configuration() {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(CONFIG_FILENAME);
			load(fis);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (fis != null)
				closeSilently(fis);
		}
	}

	public static final Configuration getInstance() {
		return ConfigurationSingleton.INSTANCE;
	}

	private static final void closeSilently(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
