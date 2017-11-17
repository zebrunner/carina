package com.qaprosoft.apitools.util;

import java.util.Properties;

public class PropertiesUtil {

	public static Properties readProperties(String path) {
		Properties prop = new Properties();
		try {
			prop.load(PropertiesUtil.class.getClassLoader().getResourceAsStream(path));
		} catch (Exception e) {
			throw new RuntimeException("Can't read properties from file", e);
		}
		return prop;
	}

}
