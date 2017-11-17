package com.qaprosoft.apitools.builder;

public enum PropertiesKeywords {

	GENERATE_WORD_REGEX("generate_word\\(\\d+\\)"),
	GENERATE_NUMBER_REGEX("generate_number\\(\\d+\\)"),
	GENERATE_DATE_REGEX("generate_date\\(.+;-{0,1}\\d+\\)");

	private String key;

	private PropertiesKeywords(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
