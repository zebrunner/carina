package com.qaprosoft.carina.core.foundation.utils.resources;

import org.apache.commons.lang3.StringUtils;

public class ClassLoaderUtil {

	/**
	 * With some Maven plugins usage (e.g. exec-maven-plugin) SystemClassLoader can
	 * not 'see' project resources. That's why we need a possibility to choose what
	 * ClassLoader instance to use.
	 * 
	 * @return ClassLoader instance depending on 'carina_class_loader' System
	 *         property
	 */
	public static ClassLoader initClassLoader() {
		String carinaClassLoader = System.getProperty("carina_class_loader");
		if (!StringUtils.isEmpty(carinaClassLoader) && Boolean.valueOf(carinaClassLoader)) {
			return ClassLoaderUtil.class.getClassLoader();
		}
		return ClassLoader.getSystemClassLoader();
	}

}
