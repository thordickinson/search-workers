package com.thordickinson.searchworkers.stream;

import lombok.Getter;

/**
 * Allows to test a Char stream just by setting a constant string.
 */
public class ConstantStringCharStream implements CharStream{

    private final String value;
    private int index = 0;
    @Getter
    private boolean looping = false;

    /**
     * Creates a non {@link #isLooping() looping} stream
     * @param value the string that contains the chars this stream will return.
     */
    public ConstantStringCharStream(String value){
        this(value, false);
    }

    /**
     * Creates a char stream that will returns every character of the given string.
     * @param value the string that contains the characters this stream will return.
     * @param looping if true, the chars of the value will be returned in a loop.
     */
    public ConstantStringCharStream(String value, boolean looping){
        this.value = value;
        this.looping = looping;
    }

    @Override
    public char next() {
        if(index > value.length() - 1){
            if(!looping){
                return END;
            }
            index = 0; //Looping
        }
        return value.charAt(index++);
    }
}
