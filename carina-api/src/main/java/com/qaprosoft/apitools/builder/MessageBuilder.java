package com.qaprosoft.apitools.builder;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class MessageBuilder {

	private static Configuration freemarkerConfiguration;

	static {
		freemarkerConfiguration = new Configuration();
		freemarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(MessageBuilder.class, "/"));
	}

	public final static synchronized String buildStringMessage(String templatePath, Properties... propertiesArr) {
		Template template;
		try {
			template = freemarkerConfiguration.getTemplate(templatePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Properties resultProperties = new Properties();
		for (Properties properties : propertiesArr) {
			resultProperties.putAll(properties);
		}

		StringWriter sw = new StringWriter();
		try {
			template.process(resultProperties, sw);
		} catch (TemplateException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				sw.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return sw.getBuffer().toString();
	}
}
