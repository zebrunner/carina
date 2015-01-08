package com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.exceptions;

/**
 * Created by Patotsky on 08.01.2015.
 */

public class GroupByException extends RuntimeException {

    public GroupByException() {
    }

    public GroupByException(String message) {
        super(message);
    }

    public GroupByException(String message, Throwable cause) {
        super(message, cause);
    }

    public GroupByException(Throwable cause) {
        super(cause);
    }

    public GroupByException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
