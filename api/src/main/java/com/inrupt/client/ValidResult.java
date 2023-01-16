package com.inrupt.client;

public class ValidResult {

    private boolean valid;
    private String message;

    public ValidResult(final boolean valid){
        this(valid, null);
    }

    public ValidResult(final boolean valid, final String message){
        this.valid = valid;
        this.message = message;
    }

    public boolean getValid(){
        return this.valid;
    }

    public String getMessage(){
        return this.message;
    }
}
