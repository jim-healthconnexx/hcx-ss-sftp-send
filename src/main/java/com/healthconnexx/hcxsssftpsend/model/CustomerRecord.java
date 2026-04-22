package com.healthconnexx.hcxsssftpsend.model;

/**
 * HDC-24: Immutable record representing a customer row from the healthdata.customer table.
 * Column mapping:
 *   customer_id                → customerId
 *   bucket                     → bucket
 *   // HDC-30: outgoing_request_location → outgoingRequestLocation (source for SFTP delivery)
 *   outgoing_request_location  → outgoingRequestLocation
 *   request_processed_location → requestProcessedLocation (@Deprecated — was incorrectly used as source)
 *   request_sent_location      → requestSentLocation
 */
public record CustomerRecord(
        int customerId,
        String bucket,
        // HDC-30: added — the folder where outgoing files await SFTP delivery
        String outgoingRequestLocation,
        // HDC-30: @Deprecated — was incorrectly used as sourceKey; retained for reference
        @Deprecated String requestProcessedLocation,
        String requestSentLocation) {
}

