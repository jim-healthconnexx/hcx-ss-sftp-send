package com.healthconnexx.hcxsssftpsend.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

/**
 * HDC-24: Transactional writer for updating panel status in healthdata.panel.
 * Schema is set via JDBC URL (currentSchema=healthdata); no schema prefix in jOOQ calls.
 */
@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PanelStatusWriter {

    private final DSLContext dsl;

    /**
     * HDC-24: Update the status of a single panel row.
     * Called after SFTP send and S3 move both succeed.
     *
     * @param panelId   the panel to update
     * @param newStatus the target status (e.g., "Request Sent")
     */
    public void updateStatus(int panelId, String newStatus) {
        log.debug("HDC-24: Updating panel status panelId={} newStatus={}", panelId, newStatus);
        int updated = dsl.update(table(name("panel")))
                .set(field(name("status")), newStatus)
                .where(field(name("panel_id")).eq(panelId))
                .execute();

        if (updated == 0) {
            log.warn("HDC-24: No panel row updated — panelId={} may not exist", panelId);
        } else {
            log.debug("HDC-24: Panel status updated panelId={} newStatus={}", panelId, newStatus);
        }
    }
}

