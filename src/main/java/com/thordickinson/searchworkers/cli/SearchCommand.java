package com.thordickinson.searchworkers.cli;

import com.thordickinson.searchworkers.stream.CharStream;
import com.thordickinson.searchworkers.stream.ConstantStringCharStream;
import com.thordickinson.searchworkers.stream.RandomCharStream;
import com.thordickinson.searchworkers.task.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

/**
 * This class handles the logic of the main command of the application.
 */
@CommandLine.Command(name="stream-search", mixinStandardHelpOptions = true, description = "Finds for a string into a stream of characters group")
public class SearchCommand implements Runnable{

    private final ApplicationContext ctx;
    private static final Logger LOG = LoggerFactory.getLogger(SearchCommand.class);

    public SearchCommand(ApplicationContext ctx){
        this.ctx = ctx;
    }

    @CommandLine.Option(names = {"-s", "--target-string"}, description = "Allows to change the to find")
    private String targetString = "Lpfn";

    @CommandLine.Option(names = { "-t", "--timeout"}, description = "Sets the timeout value in seconds")
    private Integer timeout = 60;

    @CommandLine.Option(names = { "-f", "--timeout-fail"}, description = "Forces a timeout fail")
    private Boolean timeoutFail = false;


    @CommandLine.Option(names = { "-n", "--thread-count"}, description = "Number of concurrent threads to run")
    private Integer threadCount = 10;

    @Override
    public void run() {
        TaskService service = ctx.getBean(TaskService.class);
        LOG.info("Running {} threads", threadCount);
        if(timeoutFail){
            LOG.warn("Forcing a timeout failure");
        }
        for(int i = 0; i < threadCount; i++){
            CharStream stream = timeoutFail? new ConstantStringCharStream("a", true)  : new RandomCharStream();
            service.addTask("Task " + i, stream, targetString, timeout * 1000);
        }
        service.runTasks();
    }
}
