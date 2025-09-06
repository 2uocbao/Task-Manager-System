package com.quocbao.taskmanagementsystem.exception;

import java.io.Serial;

public class DuplicateException extends RuntimeException {

    /**
     * 
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public DuplicateException(String message) {
        super(message);
    }

}
