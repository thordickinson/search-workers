package com.thordickinson.searchworkers.task;

import com.thordickinson.searchworkers.stream.CharStream;
import com.thordickinson.searchworkers.util.LimitedQueue;
import lombok.Getter;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Searches for a string inside of a {@link CharStream}.
 */
public class StreamSearcher implements Runnable {

    private final CharStream stream;
    /**
     * If true, we need to stop the main task loop.
     */
    private boolean stopped = false;
    /**
     * If true, the task was stopped by a timeout event.
     */
    private boolean timeout = false;
    /**
     * Indicates if the main task loop is running.
     */
    @Getter
    private boolean running = false;
    /**
     * Stores the time when this task started.
     */
    private long startTime;
    /**
     * Holds the count of bytes read.
     * As we don't really know the size of the stream, we'll use a BigInteger.
     */
    private BigInteger byteCount = BigInteger.ZERO;
    /**
     * The string we're trying to find.
     */
    private final String targetString;
    /**
     * Stores the last n characters of the stream, where n is the length of the {@link #targetString}
     */
    private final LimitedQueue<Character> buffer;
    /**
     * All the listeners of the termination event.
     */
    private final List<TaskEndListener> listeners = new LinkedList<>();
    /**
     * Is the id of this task.
     */
    @Getter
    private final String taskId;

    /**
     * Creates a new Task
     * @param taskId the id of the task, just for the parent to identify the task, not used for logic.
     * @param stream the stream to read from
     * @param targetString the string we're looking for
     */
    public StreamSearcher(String taskId, CharStream stream, String targetString) {
        this.stream = stream;
        this.taskId = taskId;
        this.targetString = targetString;
        buffer = new LimitedQueue<>(targetString.length());
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        byteCount = BigInteger.ZERO;
        boolean found = false;
        timeout = false;
        Exception error = null;
        char next;

        try {
            running = true;
            while (((next = stream.next()) != CharStream.END) && !stopped) {
                buffer.add(next);
                byteCount = byteCount.add(BigInteger.ONE);
                if (compareBuffer()) {
                    found = true;
                    this.stop(false);
                }
            }
        } catch(Exception ex){
            error = ex; //Don't want to log the error here, will be handled by the listener.
        } finally {
            running = false;
        }

        TaskResultStatus status = found ? TaskResultStatus.SUCCESS : timeout ? TaskResultStatus.TIMEOUT : TaskResultStatus.FAILURE;
        fireEndEvent(status, Optional.ofNullable(error));
    }

    /**
     * Checks if the current buffer is equals to the searched string.
     * @return true, if the strings are equals.
     */
    private boolean compareBuffer() {
        if (buffer.size() != targetString.length()) return false;
        int index = 0;
        Iterator<Character> iterator = buffer.iterator();
        while (iterator.hasNext()) {
            if(targetString.charAt(index++) != iterator.next()){
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a listener for the task end event.
     * @param listener
     */
    public void addTaskEndListener(TaskEndListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * Fires the termination event to listeners
     * @param status
     */
    private void fireEndEvent(TaskResultStatus status, Optional<Exception> error) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        TaskEndEvent event = new TaskEndEvent(taskId, status, byteCount, elapsedTime, error);
        for (TaskEndListener listener : this.listeners) {
            listener.taskEnded(event);
        }
    }

    /**
     * Stops the execution of the task if is running.
     * @param isTimeout if true, the termination will be marked as a timeout.
     */
    public void stop(boolean isTimeout) {
        if (!stopped) {
            stopped = true;
            timeout = isTimeout;
        }
    }
}
