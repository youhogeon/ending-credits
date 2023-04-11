package com.youhogeon.credits.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.youhogeon.credits.entity.Config;
import com.youhogeon.credits.repository.ConfigRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfigService {
    
    private final ConfigRepository configRepository;

    public LocalDateTime getLastCredits() {
        String lastCredits = getValue("lastCredits");

        if (lastCredits == null) return LocalDateTime.of(2023, 1, 1, 0, 0);

        return LocalDateTime.parse(lastCredits);
    }

    public LocalDateTime getLastCreditsForHtml() {
        return getLastCredits().truncatedTo(ChronoUnit.SECONDS);
    }

    public void setLastCredits(LocalDateTime lastCredits) {
        setValue("lastCredits", lastCredits.toString());
    }

    public String getHeader() {
        String header = getValue("header");

        return header == null ? "도움 주신 분들" : header;
    }

    public void setHeader(String header) {
        setValue("header", header);
    }

    public String getFooter() {
        String footer = getValue("footer");

        return footer == null ? "감사합니다." : footer;
    }

    public void setFooter(String footer) {
        setValue("footer", footer);
    }

    public String getToonationKey() {
        return getValue("toonationKey");
    }

    public void setToonationKey(String toonationKey) {
        setValue("toonationKey", toonationKey);
    }

    public String getTwipKey() {
        return getValue("twipKey");
    }

    public void setTwipKey(String twipKey) {
        setValue("twipKey", twipKey);
    }

    private String getValue(String key) {
        Optional<Config> config = configRepository.findById(key);

        if (config.isEmpty()) return null;

        return config.get().getValue();
    }

    private void setValue(String key, String value) {
        Config config = new Config();
        config.setKey(key);
        config.setValue(value);

        configRepository.save(config);
    }
}
