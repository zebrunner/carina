package com.qaprosoft.carina.core.foundation.exception;

public class RequiredCtorNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -8715912005469790072L;

    public RequiredCtorNotFoundException() {
        super("Required constructor isn't found.");
    }

    public RequiredCtorNotFoundException(String msg) {
        super("Required constructor isn't found: " + msg);
    }
}
