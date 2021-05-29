package com.thordickinson.searchworkers.task;

import lombok.Data;

import java.math.BigInteger;
import java.util.Optional;

/**
 * This is an event that contains all the info about a Task termination.
 */
@Data //Generates getters, setters and constructor
public class TaskEndEvent implements Comparable<TaskEndEvent> {
    /**
     * Id of the task that have ended.
     */
    private final String taskId;
    /**
     * The status of the task
     */
    private final TaskResultStatus status;
    /**
     * Amount of bytes this task read from stream
     */
    private final BigInteger byteCount;
    /**
     * Total time that the task took to complete
     */
    private final long elapsedTimeMs;
    /**
     * If {@link #getStatus()} is a FAILURE, this variable might have the cause.
     */
    private final Optional<Exception> error;

    @Override
    public int compareTo(TaskEndEvent o) {
        return elapsedTimeMs > o.elapsedTimeMs? 1: elapsedTimeMs < o.elapsedTimeMs? -1 : 0;
    }
}
