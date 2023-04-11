package com.youhogeon.credits.controller;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.youhogeon.credits.dto.ConfigRequestDto;
import com.youhogeon.credits.dto.CountResponseDto;
import com.youhogeon.credits.service.ConfigService;
import com.youhogeon.credits.service.DonationService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ConfigController {
    
    private final DonationService donationService;
    private final ConfigService configService;

    @GetMapping("/api/count")
    public CountResponseDto count(@RequestParam("from") LocalDateTime from) {
        long expectedCount = donationService.expectedCount(from);

        CountResponseDto countResponseDto = new CountResponseDto();
        countResponseDto.setExpectedCount(expectedCount);

        return countResponseDto;
    }

    @PostMapping("/api/config")
    public void config(ConfigRequestDto configRequestDto, HttpServletResponse response) throws IOException {
        configService.setLastCredits(configRequestDto.getLastCredits());
        configService.setHeader(configRequestDto.getHeader());
        configService.setFooter(configRequestDto.getFooter());
        configService.setToonationKey(configRequestDto.getToonationKey());
        configService.setTwipKey(configRequestDto.getTwipKey());

        response.sendRedirect("/");
    }

}
