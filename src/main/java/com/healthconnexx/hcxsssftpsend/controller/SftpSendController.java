package com.healthconnexx.hcxsssftpsend.controller;

import com.healthconnexx.hcxsssftpsend.service.SftpSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HDC-24: REST endpoint to trigger SFTP file send for a customer.
 */
@RestController
@RequestMapping("/api/v1/sftp")
@RequiredArgsConstructor
@Slf4j
public class SftpSendController {

    private final SftpSendService sftpSendService;

    /**
     * HDC-24: Trigger the SFTP send process for the given customer.
     * Queries all panels in "Request Created" status, downloads files from S3,
     * sends via SFTP, moves S3 files to sent location, and updates panel status.
     *
     * @param request body containing the customerId
     * @return 200 OK on success
     */
    @PostMapping("/send")
    public ResponseEntity<Void> send(@RequestBody SftpSendRequest request) {
        log.info("HDC-24: Received SFTP send request for customerId={}", request.customerId());
        sftpSendService.sendFiles(request.customerId());
        return ResponseEntity.ok().build();
    }

    /** HDC-24: Request body for POST /api/v1/sftp/send */
    public record SftpSendRequest(int customerId) {
    }
}

