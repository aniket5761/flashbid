package com.example.flashbid.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

@Component
@RequiredArgsConstructor
public class ApplicationTimeZoneConfig {

    @Value("${app.timezone:Asia/Kolkata}")
    private String applicationTimeZone;

    @PostConstruct
    public void configureTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(applicationTimeZone));
    }
}
