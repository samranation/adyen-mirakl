package com.adyen.mirakl.exceptions;

public class UnexpectedMailFailureException extends RuntimeException{

    public UnexpectedMailFailureException() {
    }

    public UnexpectedMailFailureException(final String message) {
        super(message);
    }

    public UnexpectedMailFailureException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UnexpectedMailFailureException(final Throwable cause) {
        super(cause);
    }

    public UnexpectedMailFailureException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
