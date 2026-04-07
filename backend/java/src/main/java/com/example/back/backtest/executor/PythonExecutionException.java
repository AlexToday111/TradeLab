package com.example.back.backtest.executor;

public class PythonExecutionException extends RuntimeException {
    public PythonExecutionException(String message) {
        super(message);
    }

    public PythonExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}