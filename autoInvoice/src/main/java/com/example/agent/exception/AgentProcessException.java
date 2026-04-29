package com.example.agent.exception;

/**
 * Agent处理异常
 */
public class AgentProcessException extends RuntimeException {

    public AgentProcessException(String message) {
        super(message);
    }

    public AgentProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
