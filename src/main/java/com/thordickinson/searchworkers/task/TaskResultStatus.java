package com.thordickinson.searchworkers.task;

/**
 * Result status enum
 */
public enum TaskResultStatus {
    /**
     * The task found the string
     */
    SUCCESS,
    /**
     * The task was unable to find the string in the
     * required time
     */
    TIMEOUT,
    /**
     * The task failed due to an exception or because the string
     * was not found in the stream
     */
    FAILURE
}
