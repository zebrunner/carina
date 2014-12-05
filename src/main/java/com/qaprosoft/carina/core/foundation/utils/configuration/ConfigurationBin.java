package com.qaprosoft.carina.core.foundation.utils.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "arg" })
@XmlRootElement(name = "config")
public class ConfigurationBin {

	protected List<ArgumentType> arg;

	public List<ArgumentType> getArg() {
		if (arg == null) {
			arg = new ArrayList<ArgumentType>();
		}
		return this.arg;
	}
}
