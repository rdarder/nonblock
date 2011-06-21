package com.globant.nonblock.netty.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton placeholder for bootstrap parameters.
 * 
 * @author Julian Gutierrez Oschmann
 * 
 */
public class BootstrapParameters {

	/**
	 * Command line arguments.
	 */
	private static String[] args;

	private static Properties globalProperties = new Properties();

	public static void setArgs(final String[] args) {
		BootstrapParameters.args = args;
	}

	public static Properties getProperties() {
		processCommandLineArguments(args);
		return globalProperties;
	}

	private static void processCommandLineArguments(final String[] args) {

		assert args != null : "args parameter cant be null";

		for (String s : args) {
			String[] values = s.split("=");
			String key = values[0].substring(2);
			String value = values[1];

			if (key.equals("config")) {
				Properties p = loadPropertyFile(value);
				mergeProperties(globalProperties, p);
			}
			globalProperties.put(key, value);
		}

	}

	private static void mergeProperties(final Properties original, final Properties p) {
		assert original != null : "original properties parameter cant be null";
		assert p != null : "new properties properties cant be null";
		original.putAll(p);
	}

	private static Properties loadPropertyFile(final String value) {
		try {
			File f = new File(value);
			InputStream valuesFile = new FileInputStream(f);
			Properties p = new Properties();
			p.load(valuesFile);
			return p;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
