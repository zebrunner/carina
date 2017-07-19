package com.qaprosoft.carina.core.foundation.exception;

/*
 * Exception may be thrown when exception in data loading occurred.
 * 
 * @author Alex Khursevich
 */
public class DataLoadingException extends RuntimeException
{
	private static final long serialVersionUID = -6264855148555485530L;

	public DataLoadingException()
	{
		super("Can't load data.");
	}

	public DataLoadingException(String msg)
	{
		super("Can't load data: " + msg);
	}
}