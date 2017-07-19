package com.qaprosoft.carina.core.foundation.utils.android.recorder.exception;

/**
 * Created by YP.
 * Date: 8/19/2014
 * Time: 12:35 AM
 */
public class ExecutorException extends Exception{

    private static final long serialVersionUID = -2841173595515246802L;

    public ExecutorException() {
        super();
    }

    public ExecutorException(String message) {
        super(message);
    }

    public ExecutorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecutorException(Throwable cause) {
        super(cause);
    }

}
