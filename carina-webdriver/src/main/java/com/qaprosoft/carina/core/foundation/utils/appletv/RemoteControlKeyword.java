package com.qaprosoft.carina.core.foundation.utils.appletv;

public enum RemoteControlKeyword {

	HOME("home"), 
	LEFT("left"), 
	RIGHT("right"), 
	UP("up"), 
	DOWN("down"),
	MENU("menu"), 
	SELECT("select"),
	PLAY("playpause");

	private String controlKeyword;

	private RemoteControlKeyword(String controlKeyword) {
		this.setControlKeyword(controlKeyword);
	}

	public String getControlKeyword() {
		return controlKeyword;
	}

	public void setControlKeyword(String controlKeyword) {
		this.controlKeyword = controlKeyword;
	}
}