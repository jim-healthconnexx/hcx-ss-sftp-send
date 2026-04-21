package com.healthconnexx.hcxsssftpsend.model;

/**
 * HDC-24: Immutable record representing a customer row from the healthdata.customer table.
 * Column mapping:
 *   customer_id              → customerId
 *   bucket                   → bucket
 *   request_processed_location → requestProcessedLocation
 *   request_sent_location    → requestSentLocation
 */
public record CustomerRecord(
        int customerId,
        String bucket,
        String requestProcessedLocation,
        String requestSentLocation) {
}

