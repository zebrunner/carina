package com.qaprosoft.carina.core.foundation.exception;

public class InvalidConfigurationException extends RuntimeException
{
	private static final long serialVersionUID = 8617043525402250600L;
	
	public InvalidConfigurationException(Exception e)
	{
		super(e);
	}
	
	public InvalidConfigurationException(String message)
	{
		super(message);
	}
}
