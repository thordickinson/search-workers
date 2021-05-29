package com.thordickinson.searchworkers.stream;

import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Char stream implementation that generates random characters.
 */
@Service
public class RandomCharStream implements CharStream {

    /**
     * Used to generate random chars
     */
    private final Random  rand = new Random();
    /**
     * A string that contains all the possible characters this stream can return.
     */
    public static final String AVAILABLE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxys";
    /**
     * @return a pseudo-random character from the {@link #AVAILABLE_CHARS}.
     */
    @Override
    public char next(){
        return AVAILABLE_CHARS.charAt(rand.nextInt(AVAILABLE_CHARS.length()));
    }

}
