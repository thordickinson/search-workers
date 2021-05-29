package com.thordickinson.searchworkers.task;

import com.thordickinson.searchworkers.stream.CharStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service in charge to schedule and monitor tasks
 */
@Service
public class TaskService implements  TaskEndListener{

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    private int threadPoolSize = 10;
    /**
     * Service for running the stream check tasks
     */
    private ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
    /**
     * Service used to run the timeout checking tasks.
     */
    private ScheduledExecutorService timeoutService = Executors.newSingleThreadScheduledExecutor();
    /**
     * Used for counting the active tasks.
     */
    private final AtomicInteger runningTaskCount = new AtomicInteger();
    /**
     * Stores the results while the task ends, then is used to print them all.
     */
    //Using a thread-safe implementation of a list to store results
    private final LinkedBlockingDeque<TaskEndEvent> results = new LinkedBlockingDeque<>();

    public StreamSearcher search(String taskId, CharStream stream, String targetString, long timeoutMs){
        LOG.info("New worker started - Searching for string '{}' Timeout: {}ms", targetString, timeoutMs);
        StreamSearcher searcher = new StreamSearcher(taskId, stream, targetString);
        final ScheduledFuture<?> timeoutFuture = timeoutService.schedule(new TimeoutTask(searcher), timeoutMs, TimeUnit.MILLISECONDS);

        //Stops the timeout timer when the task completes
        searcher.addTaskEndListener(e -> {
            timeoutFuture.cancel(true);
            int runningTasks = runningTaskCount.decrementAndGet();
            LOG.debug("Stopping timeout timer for task - Running Tasks {}", runningTasks);
            if(runningTasks == 0){
                timeoutService.shutdownNow();
                printResults();
            }
        });
        searcher.addTaskEndListener(this);
        runningTaskCount.incrementAndGet();
        executor.submit(searcher);
        return searcher;
    }

    /**
     * Print the final results.
     */
    private void printResults(){
        LOG.info("Here comes the result");
        results.stream().forEach(r -> LOG.info("{} -> Elapsed: {}ms - Byte Count: {} - Status: {}", r.getTaskId(), r.getElapsedTimeMs(), r.getByteCount(), r.getStatus()));
    }

    /**
     * Method invoked when a task failure is detected
     * @param event
     */
    protected void onTaskFailure(TaskEndEvent event){
        LOG.warn("Task failed in {}ms - [{} bytes processed]", event.getElapsedTimeMs(), event.getByteCount());
    }

    /**
     * Method invoked when a task completes
     * @param event
     */
    protected void onTaskSuccess(TaskEndEvent event){
        LOG.debug("Task successfully completed in {}ms - [{} bytes processed]", event.getElapsedTimeMs(), event.getByteCount());
    }

    /**
     * Method invoked on a task ends by a timeout
     * @param event
     */
    protected void onTaskTimeout(TaskEndEvent event){
        LOG.warn("Task timed out in {}ms - [{} bytes processed]", event.getElapsedTimeMs(), event.getByteCount());
    }

    @Override
    public void taskEnded(TaskEndEvent event) {
        TaskResultStatus status = event.getStatus();
        results.push(event);
        if(status == TaskResultStatus.FAILURE){
            onTaskFailure(event);
        }else if(status == TaskResultStatus.SUCCESS){
            onTaskSuccess(event);
        }else if(status == TaskResultStatus.TIMEOUT){
            onTaskTimeout(event);
        }
    }

    /**
     * Stops the executors. This method doesn't stops the task when invoked, just indicates to the
     * executor services to stop when the scheduled tasks ends.
     */
    public void shutdown(){
        executor.shutdown();
        timeoutService.shutdown();
    }
}
