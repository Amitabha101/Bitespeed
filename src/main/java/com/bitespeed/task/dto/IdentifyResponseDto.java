package com.bitespeed.task.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IdentifyResponseDto {
    private ContactDTO contact;
}
