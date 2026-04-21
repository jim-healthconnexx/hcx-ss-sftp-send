package com.healthconnexx.hcxsssftpsend.reader;

import com.healthconnexx.hcxsssftpsend.model.PanelRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

/**
 * HDC-24: Read-only jOOQ queries against healthdata.panel.
 * Schema is set via JDBC URL (currentSchema=healthdata); no schema prefix in jOOQ calls.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PanelReader {

    private final DSLContext dsl;

    /**
     * HDC-24: Find all panels for a customer in a particular status.
     *
     * @param customerId the customer whose panels to query
     * @param status     the panel status to filter on (e.g., "Request Created")
     * @return list of matching PanelRecords
     */
    public List<PanelRecord> findByCustomerIdAndStatus(int customerId, String status) {
        log.debug("HDC-24: Querying panels for customerId={} status={}", customerId, status);
        return dsl.select(
                        field(name("panel_id")),
                        field(name("customer_id")),
                        field(name("sent_request_filename"))
                )
                .from(table(name("panel")))
                .where(
                        field(name("customer_id")).eq(customerId)
                                .and(field(name("status")).eq(status))
                )
                .fetch(r -> new PanelRecord(
                        r.get(field(name("panel_id")), Integer.class),
                        r.get(field(name("customer_id")), Integer.class),
                        r.get(field(name("sent_request_filename")), String.class)
                ));
    }
}

