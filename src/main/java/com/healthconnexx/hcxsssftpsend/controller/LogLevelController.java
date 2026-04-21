package com.healthconnexx.hcxsssftpsend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HDC-24: Runtime log-level control — identical pattern to HDC-18.
 * Delegates to Spring Boot Actuator's LoggersEndpoint so changes are
 * also visible via /actuator/loggers and vice versa.
 *
 * Usage:
 *   PUT  /api/v1/admin/log-level   body: {"loggerName":"com.healthconnexx","level":"DEBUG"}
 *   GET  /api/v1/admin/log-level?loggerName=com.healthconnexx
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class LogLevelController {

    private final LoggersEndpoint loggersEndpoint;

    /**
     * HDC-24: Change the log level of a named logger at runtime (no restart required).
     *
     * @param request body containing loggerName and level
     * @return 200 OK
     */
    @PutMapping("/log-level")
    public ResponseEntity<Void> setLogLevel(@RequestBody LogLevelRequest request) {
        log.info("HDC-24: Setting log level loggerName={} level={}", request.loggerName(), request.level());
        loggersEndpoint.configureLogLevel(request.loggerName(), LogLevel.valueOf(request.level().toUpperCase()));
        return ResponseEntity.ok().build();
    }

    /**
     * HDC-24: Get the current effective and configured log level for a named logger.
     *
     * @param loggerName the logger to inspect
     * @return logger level descriptor
     */
    @GetMapping("/log-level")
    public ResponseEntity<Object> getLogLevel(@RequestParam String loggerName) {
        log.debug("HDC-24: Getting log level for loggerName={}", loggerName);
        return ResponseEntity.ok(loggersEndpoint.loggerLevels(loggerName));
    }

    /** HDC-24: Request body for PUT /api/v1/admin/log-level */
    public record LogLevelRequest(String loggerName, String level) {
    }
}

