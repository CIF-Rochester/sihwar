package com.vicious.sihwar.commands;

/**
 * Should be thrown by commands when an unexpected exception occurs. Prints the error to console.
 */
public class ConsoleCommandException extends CommandException{
    public ConsoleCommandException() {
    }

    public ConsoleCommandException(String message) {
        super(message);
    }

    public ConsoleCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsoleCommandException(Throwable cause) {
        super(cause);
    }

    public ConsoleCommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
