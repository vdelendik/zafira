package com.qaprosoft.zafira.services.exceptions;

public class InvalidTestRunException extends ServiceException
{
	private static final long serialVersionUID = 2071417461140045873L;

	public InvalidTestRunException()
	{
		super();
	}

	public InvalidTestRunException(String message)
	{
		super(message);
	}

	public InvalidTestRunException(Throwable cause)
	{
		super(cause);
	}

	public InvalidTestRunException(String message, Throwable cause)
	{
		super(message, cause);
	}
}