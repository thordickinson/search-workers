package com.thordickinson.searchworkers.task;

import com.thordickinson.searchworkers.stream.CharStream;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Service in charge to schedule and monitor tasks
 */
@Service
public class TaskService implements  TaskEndListener{

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);
    /**
     * Service for running the stream check tasks
     */
    private ExecutorService executor;
    /**
     * Service used to run the timeout checking tasks.
     */
    private ScheduledExecutorService timeoutService;
    /**
     * Used for counting the active tasks.
     */
    private AtomicInteger runningTaskCount;

    private final Map<String, TaskWrapper> tasks = new ConcurrentHashMap<>();

    /**
     * Add a stream search task. The task wont start until the {@link #runTasks()} method is called.
     * @param taskId a id for the task
     * @param stream the stream in which the task will look for the targetString
     * @param targetString the string that will be searched on the stream
     * @param timeoutMs timeout for the task
     * @return
     */
    public StreamSearcher addTask(String taskId, CharStream stream, String targetString, long timeoutMs){
        LOG.info("New worker started - Searching for string '{}' Timeout: {}ms", targetString, timeoutMs);
        StreamSearcher searcher = new StreamSearcher(taskId, stream, targetString);
        searcher.addTaskEndListener(this);
        tasks.put(taskId, new TaskWrapper(searcher, timeoutMs));
        return searcher;
    }

    /**
     * Starts all the scheduled tasks.
     */
    public void runTasks(){
        runningTaskCount = new AtomicInteger(tasks.size());
        executor = Executors.newFixedThreadPool(runningTaskCount.get());
        timeoutService = Executors.newSingleThreadScheduledExecutor();

        LOG.info("Starting {}  tasks", runningTaskCount.get());

        tasks.values().forEach(t -> {
            if(t.getTimeoutMs() > 1000){ //At least a second
                ScheduledFuture<?> timeoutFuture = timeoutService.schedule(new TimeoutTask(t.getSearcher()), t.getTimeoutMs(), TimeUnit.MILLISECONDS);
                t.setTimeoutTask(Optional.of(timeoutFuture));
            }
            Future<?> task = executor.submit(t.getSearcher());
            t.setTaskFuture(Optional.of(task));
        });
    }

    /**
     * Print the final results.
     */
    private void printResults(){
        //Using println only in this method!!
        System.out.format("RESULT\n%-10s%10s%15s%13s\n", "Task", "Elapsed", "Bytes Read", "Status");
        Stream<TaskEndEvent> sortedResults = tasks.values().stream().map(w -> w.getResult()).map(r -> r.orElseThrow(IllegalStateException::new)).sorted();
        sortedResults.forEach(r -> System.out.format("%s%10dms%15d%15s\n",r.getTaskId(), r.getElapsedTimeMs(), r.getByteCount(), r.getStatus()));
    }

    /**
     * Method invoked when a task failure is detected
     * @param wrapper
     */
    protected void onTaskFailure(TaskWrapper wrapper){
        TaskEndEvent event = wrapper.getResult().get();
        LOG.warn("Task failed in {}ms - [{} bytes processed]", event.getElapsedTimeMs(), event.getByteCount());
    }

    /**
     * Method invoked when a task completes
     * @param wrapper
     */
    protected void onTaskSuccess(TaskWrapper wrapper){
        TaskEndEvent event = wrapper.getResult().get();
        LOG.debug("Task successfully completed in {}ms - [{} bytes processed]", event.getElapsedTimeMs(), event.getByteCount());
    }

    /**
     * Method invoked on a task ends by a timeout
     * @param wrapper
     */
    protected void onTaskTimeout(TaskWrapper wrapper){
        TaskEndEvent event = wrapper.getResult().get();
        LOG.warn("Task timed out in {}ms - [{} bytes processed]", event.getElapsedTimeMs(), event.getByteCount());
    }

    @Override
    public void taskEnded(TaskEndEvent event) {
        TaskWrapper wrapper = tasks.get(event.getTaskId());
        wrapper.setResult(Optional.of(event));

        TaskResultStatus status = event.getStatus();
        if(status == TaskResultStatus.FAILURE){
            onTaskFailure(wrapper);
        }else if(status == TaskResultStatus.SUCCESS){
            onTaskSuccess(wrapper);
        }else if(status == TaskResultStatus.TIMEOUT){
            onTaskTimeout(wrapper);
        }

        int runningTasksCount = runningTaskCount.decrementAndGet();
        if(runningTasksCount == 0){
            shutdown();
            printResults();
        }
    }

    /**
     * Stops the executors. This method doesn't stops the task when invoked, just indicates to the
     * executor services to stop when the scheduled tasks ends.
     */
    public void shutdown(){
        executor.shutdownNow();
        timeoutService.shutdownNow();
    }
}

@Data
class TaskWrapper {
    private final StreamSearcher searcher;
    private final long timeoutMs;
    private Optional<Future> timeoutTask = Optional.empty();
    private Optional<Future> taskFuture = Optional.empty();
    private Optional<TaskEndEvent> result = Optional.empty();
}