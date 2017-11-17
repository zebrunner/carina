package com.qaprosoft.apitools.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesProcessorMain {

	private static List<PropertiesProcessor> processors;

	static {
		processors = new ArrayList<PropertiesProcessor>();
		processors.add(new GenerateProcessor());
	}

	public static Properties processProperties(Properties in) {
		Properties out = new Properties();
		for (PropertiesProcessor processor : processors) {
			out.putAll(processor.process(in));
		}
		return out;
	}

}
