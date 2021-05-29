package com.thordickinson.searchworkers.task;

import com.thordickinson.searchworkers.stream.CharStream;
import com.thordickinson.searchworkers.stream.ConstantStringCharStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TaskServiceTest {

    @Autowired
    private TaskService searchService;

    private void sleep(long time){
        try{
            Thread.sleep(time);
        }catch(InterruptedException ex){
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void testTimeout(){
        //Testing with a looping stream that never sends Lpfn
        CharStream stream = new ConstantStringCharStream("abcdefg", true);
        StreamSearcher searcher = searchService.addTask("Timeout", stream, "Lpfn", 2000);
        final ValueReference<TaskEndEvent> result = new ValueReference<>(null);
        searcher.addTaskEndListener(result::setValue);
        searchService.runTasks();
        sleep(1100);
        assertTrue(searcher.isRunning()); //Waiting for a second and try to test if is still running
        sleep(1100);
        assertNotNull(result.getValue());
        assertEquals(TaskResultStatus.TIMEOUT, result.getValue().getStatus());
        assertFalse(searcher.isRunning()); //It shouldn't be running
    }

    @Test
    public void testStringFound(){
        //Returning 100 characters before returning the target string, should take less than a second
        CharStream stream = new TestConstantStringCharStream(100, 'a', "Lpfn", 0);
        StreamSearcher searcher = searchService.addTask("Constant", stream, "Lpfn", 5000);
        final ValueReference<TaskEndEvent> result = new ValueReference<>(null);
        searcher.addTaskEndListener(result::setValue);
        searchService.runTasks();
        sleep(1000);
        assertNotNull(result.getValue());
        assertEquals(TaskResultStatus.SUCCESS, result.getValue().getStatus());
        assertFalse(searcher.isRunning());
    }


    @Test
    public void testStringNotFound(){
        //A short stream that doesn't contains the searched string
        CharStream stream = new ConstantStringCharStream("helloworld", false);
        StreamSearcher searcher = searchService.addTask("Constant", stream, "Lpfn", 5000);
        final ValueReference<TaskEndEvent> result = new ValueReference<>(null);
        searcher.addTaskEndListener(result::setValue);
        searchService.runTasks();
        sleep(1000);
        assertNotNull(result.getValue());
        assertEquals(TaskResultStatus.FAILURE, result.getValue().getStatus());
        assertFalse(searcher.isRunning());
    }


}