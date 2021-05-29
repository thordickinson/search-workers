package com.thordickinson.searchworkers.task;
import com.thordickinson.searchworkers.stream.CharStream;
import com.thordickinson.searchworkers.stream.ConstantStringCharStream;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class StreamSearcherTest {


    @Test
    public void testFindString(){
        CharStream stream = new ConstantStringCharStream("abcLpfnmopq");
        StreamSearcher searcher = new StreamSearcher("Find", stream, "Lpfn");
        searcher.addTaskEndListener(l -> {
            assertEquals(new BigInteger("7"), l.getByteCount());
            assertEquals(TaskResultStatus.SUCCESS, l.getStatus());
        });
        searcher.run();
    }

    @Test
    public void testStringNotFound(){
        String s = "abcLpnmopq";
        CharStream stream = new ConstantStringCharStream(s);
        StreamSearcher searcher = new StreamSearcher("find", stream, "Lpfn");
        searcher.addTaskEndListener(l -> {
            assertEquals(new BigInteger("" + s.length()), l.getByteCount());
            assertEquals(TaskResultStatus.FAILURE, l.getStatus());
        });
        searcher.run();
    }

    @Test
    public void testTimeout(){
        CharStream stream = new ConstantStringCharStream("abc", true);
        StreamSearcher searcher = new StreamSearcher("Find", stream, "Lpfn");
        searcher.addTaskEndListener(l -> {
            assertEquals(TaskResultStatus.TIMEOUT, l.getStatus());
        });
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(3000);
                searcher.stop(true);
            }catch (Exception ex){
                fail(ex);
            }
        });
        t.start();
        searcher.run();

    }
}