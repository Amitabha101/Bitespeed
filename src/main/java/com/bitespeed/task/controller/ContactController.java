package com.bitespeed.task.controller;

import com.bitespeed.task.dto.IdentifyRequestDto;
import com.bitespeed.task.dto.IdentifyResponseDto;
import com.bitespeed.task.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1")
public class ContactController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping(value = "/identify")
    public ResponseEntity<IdentifyResponseDto> identify(@RequestBody @Valid IdentifyRequestDto identifyRequestDto) {
        IdentifyResponseDto identifyResponseDto = contactService.identifyContacts(identifyRequestDto);
        System.out.println(identifyResponseDto);
        return new ResponseEntity<>(identifyResponseDto, HttpStatus.CREATED);
    }
}
