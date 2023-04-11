package com.youhogeon.credits.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ConfigRequestDto {
    
    private LocalDateTime lastCredits;
    private String header;
    private String footer;
    private String toonationKey;
    private String twipKey;

}
