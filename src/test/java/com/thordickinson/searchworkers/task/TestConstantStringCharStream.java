package com.thordickinson.searchworkers.task;

import com.thordickinson.searchworkers.stream.CharStream;

/**
 * A stream that returns a constant char named as {@link #surroundingChar} the first n invocations before
 * {@link #countBefore} and after the {@link #countBefore} + {@link #targetString}.length. After the n first
 * invocations this stream will return the {@link #targetString} until the end of it.
 */
public class TestConstantStringCharStream implements CharStream {

    private final long countBefore;
    private final char surroundingChar;
    private final String targetString;
    private long index = 0;
    private final long delay;


    public TestConstantStringCharStream(long countBefore, char surroundingChar, String targetString, long delay) {
        this.countBefore = countBefore;
        this.surroundingChar = surroundingChar;
        this.targetString = targetString;
        this.delay = delay;
    }

    @Override
    public char next() {
        delay(); //Delaying the return of the next char
        index++;
        long delta = index - countBefore;
        if(delta < 0 || delta > targetString.length()){
            return surroundingChar;
        }
        return targetString.charAt((int) delta);
    }

    private void delay(){
        if(delay == 0) return;
        try{
            Thread.sleep(delay);
        }catch (InterruptedException ex){

        }
    }
}
