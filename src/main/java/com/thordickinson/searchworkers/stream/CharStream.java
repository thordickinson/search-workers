package com.thordickinson.searchworkers.stream;

/**
 * Represents a char stream that can be read from anywhere.
 */
public interface CharStream {
    /**
     * This is the character that a stream returns when there's no more chars to read from.
     * @see #next()
     */
    char END = (char) -1;
    /**
     * @return the next char in the stream or {@link #END} if there's no more chars to read.
     */
    char next();
}
