package com.thordickinson.searchworkers.stream;

import java.util.Random;

/**
 * Char stream implementation that returns a char from a random integer value.
 */
public class RandomCharStream implements CharStream{

    private  final int maxValue;
    private final Random rand = new Random();

    public RandomCharStream(int maxValue){
        this.maxValue = maxValue;
    }

    @Override
    public char next() {
        return (char) rand.nextInt(maxValue);
    }
}
