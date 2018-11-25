package com.qaprosoft.carina.core.foundation.exception;

public class DriverPoolException extends RuntimeException {
	private static final long serialVersionUID = 5200458288468528656L;

    public DriverPoolException() {
        super("Undefined failure in DriverPool!");
    }

    public DriverPoolException(String msg) {
        super(msg);
    }
}
