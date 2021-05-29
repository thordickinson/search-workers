package com.thordickinson.searchworkers.task;

/**
 * Classes that implements this interface will listen for the event of a task termination.
 */
public interface TaskEndListener {

    /**
     * Method invoked when the task ends.
     * @param event
     */
    void taskEnded(TaskEndEvent event);

}
