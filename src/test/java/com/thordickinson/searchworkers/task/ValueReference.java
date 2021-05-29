package com.thordickinson.searchworkers.task;

import lombok.Data;

/**
 * Used to store a reference inside a final object so it can be accessed inside and outside of a lambda
 * @param <T>
 */
@Data
public class ValueReference<T> {
    private T value;

    public ValueReference(T value){
        this.value = value;
    }

}
