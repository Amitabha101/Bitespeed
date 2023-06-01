package com.bitespeed.task.exceptions;

import com.bitespeed.task.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;

@ControllerAdvice
public class CentralExeptionHandler {

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorDTO> InvalidInputExceptionHandler(InvalidInputException exception) {
        ErrorDTO dto = new ErrorDTO(HttpStatus.BAD_REQUEST, "both email and phone number is null or blank");

        dto.setDetailedMessages(Collections.singletonList(exception.getMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }
}
