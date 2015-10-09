package com.qaprosoft.carina.core.foundation.utils.configuration;

import javax.xml.bind.annotation.*;

/**
 * Created by Patotsky on 04.12.2014.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "key", "value" })
public class ArgumentType {

	@XmlElement(required = true)
	protected String key;

	@XmlAttribute(name = "unique")
	protected boolean unique;

	@XmlElement(required = true)
	protected String value;

	public String getKey() {
		return key;
	}

	public void setKey(String value) {
		this.key = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean getUnique() {
		return unique;
	}

	public void setUnique(boolean value) {
		this.unique = value;
	}
}