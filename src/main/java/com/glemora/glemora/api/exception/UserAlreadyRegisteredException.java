package com.glemora.glemora.api.exception;

public class UserAlreadyRegisteredException extends Exception{
    public UserAlreadyRegisteredException(String message){
        super(message);
    }
}
