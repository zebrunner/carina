package com.qaprosoft.carina.core.foundation.exception;

import com.qaprosoft.carina.core.foundation.utils.SpecialKeywords;

public class AlreadyPassedException extends RuntimeException {

	private static final long serialVersionUID = 4368783689361216175L;

	public AlreadyPassedException()
	{
		super(SpecialKeywords.ALREADY_PASSED);
	}
}
