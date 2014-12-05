package com.qaprosoft.carina.core.foundation.utils.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by Patotsky on 04.12.2014.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "key", "defaultKey", "value" })
public class ArgumentType {

	@XmlElement(required = true)
	protected String key;
	@XmlElement(required = true)
	protected String defaultKey;
	@XmlElement(required = true)
	protected String value;

	public String getKey() {
		return key;
	}

	public void setKey(String value) {
		this.key = value;
	}

	public String getDefaultKey() {
		return defaultKey;
	}

	public void setDefaultKey(String defaultKey) {
		this.defaultKey = defaultKey;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}