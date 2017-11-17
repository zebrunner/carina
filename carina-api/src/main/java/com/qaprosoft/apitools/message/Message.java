package com.qaprosoft.apitools.message;

public abstract class Message {
	protected String messageText;

	public Message() {
	}

	public Message(String messageText) {
		this.messageText = messageText;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

}
