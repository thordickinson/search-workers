package com.thordickinson.searchworkers.task;

/**
 * This task runs when a timeout event occurs and stops a stream checker with the
 * timeout flag.
 */
public class TimeoutTask implements Runnable{

    private final StreamSearcher searcher;

    public TimeoutTask(StreamSearcher searcher){
        this.searcher = searcher;
    }

    @Override
    public void run() {
        searcher.stop(true);
    }
}
