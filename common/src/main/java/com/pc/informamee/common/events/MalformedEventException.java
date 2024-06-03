package com.pc.informamee.common.events;

public class MalformedEventException extends Exception {
    public MalformedEventException()
    {
        super();
    }
    public MalformedEventException(String msg){
        super(msg);
    }
}
