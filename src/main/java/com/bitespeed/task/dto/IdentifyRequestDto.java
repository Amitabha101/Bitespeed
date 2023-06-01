package com.bitespeed.task.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdentifyRequestDto {
    String email;
    Long phoneNumber;
}
