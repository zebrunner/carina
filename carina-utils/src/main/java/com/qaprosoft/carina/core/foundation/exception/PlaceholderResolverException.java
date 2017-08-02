package com.qaprosoft.carina.core.foundation.exception;

public class PlaceholderResolverException extends RuntimeException
{
	private static final long serialVersionUID = -1666532382220155518L;

	public PlaceholderResolverException()
	{
		super();
	}
	
	public PlaceholderResolverException(String key)
	{
		super("Value not found by key '" + key + "'");
	}
}
