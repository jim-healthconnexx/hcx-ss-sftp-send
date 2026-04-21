package com.healthconnexx.hcxsssftpsend.model;

/**
 * HDC-24: Immutable record representing a panel row from the healthdata.panel table.
 * Column mapping:
 *   panel_id              → panelId
 *   customer_id           → customerId
 *   sent_request_filename → sentRequestFilename
 */
public record PanelRecord(
        int panelId,
        int customerId,
        String sentRequestFilename) {
}

