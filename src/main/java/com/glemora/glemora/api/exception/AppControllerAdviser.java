package com.glemora.glemora.api.exception;

import com.glemora.glemora.api.controller.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppControllerAdviser {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({UserAlreadyRegisteredException.class})
    public MessageResponse handleNFException(Exception exception){
        MessageResponse messageResponse = MessageResponse.builder()
                .message(exception.getMessage())
                .build();

        return messageResponse;
    }
}
