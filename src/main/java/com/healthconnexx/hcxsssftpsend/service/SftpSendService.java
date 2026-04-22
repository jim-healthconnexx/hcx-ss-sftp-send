package com.healthconnexx.hcxsssftpsend.service;

import com.healthconnexx.hcxsssftpsend.model.CustomerRecord;
import com.healthconnexx.hcxsssftpsend.model.PanelRecord;
import com.healthconnexx.hcxsssftpsend.reader.CustomerReader;
import com.healthconnexx.hcxsssftpsend.reader.PanelReader;
import com.healthconnexx.hcxsssftpsend.writer.PanelStatusWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

/**
 * HDC-24: Orchestrates the full SFTP send flow for a customer.
 * Flow per panel: CustomerReader → PanelReader → S3 download → SFTP send → S3 move → DB update
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SftpSendService {

    private static final String STATUS_REQUEST_CREATED = "Request Created";
    private static final String STATUS_REQUEST_SENT = "Request Sent";

    private final CustomerReader customerReader;
    private final PanelReader panelReader;
    private final AwsS3Service awsS3Service;
    private final SftpClientService sftpClientService;
    private final PanelStatusWriter panelStatusWriter;

    /**
     * HDC-24: Process all "Request Created" panels for the given customer.
     *
     * @param customerId the customer to process
     */
    public void sendFiles(int customerId) {
        log.info("HDC-24: Starting SFTP send process for customerId={}", customerId);

        CustomerRecord customer = customerReader.findById(customerId)
                .orElseThrow(() -> {
                    log.error("HDC-24: Customer not found customerId={}", customerId);
                    return new IllegalArgumentException("Customer not found: " + customerId);
                });

        List<PanelRecord> panels = panelReader.findByCustomerIdAndStatus(customerId, STATUS_REQUEST_CREATED);

        if (panels.isEmpty()) {
            log.warn("HDC-24: No panels in '{}' status for customerId={}", STATUS_REQUEST_CREATED, customerId);
            return;
        }

        log.info("HDC-24: Found {} panel(s) to send for customerId={}", panels.size(), customerId);

        for (PanelRecord panel : panels) {
            processPanel(customer, panel);
        }

        log.info("HDC-24: Completed SFTP send process for customerId={}", customerId);
    }

    // HDC-24: Process a single panel — download from S3, SFTP send, move in S3, update DB
    private void processPanel(CustomerRecord customer, PanelRecord panel) {
        if (panel.sentRequestFilename() == null || panel.sentRequestFilename().isBlank()) {
            log.warn("HDC-24: Skipping panelId={} — sentRequestFilename is null or blank", panel.panelId());
            return;
        }

        // HDC-30: @Deprecated — was customer.requestProcessedLocation() (wrong S3 location)
        String sourceKey = customer.outgoingRequestLocation()  + "/" + panel.sentRequestFilename(); // HDC-30: fixed source key
        String destKey   = customer.requestSentLocation()      + "/" + panel.sentRequestFilename();

        log.info("HDC-24: Processing panelId={} file={}", panel.panelId(), panel.sentRequestFilename());

        try {
            // Step 1: Download from S3 (outside the SFTP lock — maximises throughput)
            try (InputStream fileStream = awsS3Service.downloadFile(customer.bucket(), sourceKey)) {
                // Step 2: SFTP send (SftpClientService enforces single connection via ReentrantLock)
                sftpClientService.sendFile(fileStream, panel.sentRequestFilename());
            }

            // Step 3: Move S3 file to sent location
            awsS3Service.moveFile(customer.bucket(), sourceKey, destKey);

            // Step 4: Update panel status — only after SFTP send AND S3 move both succeed
            panelStatusWriter.updateStatus(panel.panelId(), STATUS_REQUEST_SENT);

            log.info("HDC-24: Successfully processed panelId={}", panel.panelId());

        } catch (Exception e) {
            log.error("HDC-24: Failed to process panelId={}", panel.panelId(), e);
            throw new RuntimeException("Failed to process panel " + panel.panelId(), e);
        }
    }
}

