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
        StreamSearcher searcher = searchService.search("Timeout", stream, "Lpfn", 2000);
        final ValueReference<Boolean> listenerInvoked = new ValueReference<>(false);
        searcher.addTaskEndListener(l -> {
            listenerInvoked.setValue(true);
            assertEquals(TaskResultStatus.TIMEOUT, l.getStatus());
        });
        sleep(1100);
        assertTrue(searcher.isRunning()); //Waiting for a second and try to test if is still running
        sleep(1100);
        assertTrue(listenerInvoked.getValue());
        assertFalse(searcher.isRunning()); //It shouldn't be running
    }

    @Test
    public void testStringFound(){
        //Returning 100 characters before returning the target string, should take less than a second
        CharStream stream = new TestConstantStringCharStream(1000, 'a', "Lpfn", 0);
        StreamSearcher searcher = searchService.search("Constant", stream, "Lpfn", 60000);
        sleep(1000);
        assertFalse(searcher.isRunning());
    }



}