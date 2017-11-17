package com.qaprosoft.apitools.validation;

public enum JsonCompareKeywords {

	SKIP("skip"),
	TYPE("type:"),
	REGEX("regex:"),
	ARRAY_CONTAINS("validate_array_contains_only:");

	private String key;

	private JsonCompareKeywords(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
