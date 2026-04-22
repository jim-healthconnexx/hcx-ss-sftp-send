package com.healthconnexx.hcxsssftpsend.reader;

import com.healthconnexx.hcxsssftpsend.model.CustomerRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

/**
 * HDC-24: Read-only jOOQ queries against healthdata.customer.
 * Schema is set via JDBC URL (currentSchema=healthdata); no schema prefix in jOOQ calls.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerReader {

    private final DSLContext dsl;

    /**
     * HDC-24: Find a customer by customer_id.
     *
     * @param customerId the customer to look up
     * @return an Optional containing the CustomerRecord, or empty if not found
     */
    public Optional<CustomerRecord> findById(int customerId) {
        log.debug("HDC-24: Querying customer by customerId={}", customerId);
        return dsl.select(
                        field(name("customer_id")),
                        field(name("bucket")),
                        field(name("outgoing_request_location")),   // HDC-30: source folder for SFTP delivery
                        field(name("request_processed_location")),
                        field(name("request_sent_location"))
                )
                .from(table(name("customer")))
                .where(field(name("customer_id")).eq(customerId))
                .fetchOptional(r -> new CustomerRecord(
                        r.get(field(name("customer_id")), Integer.class),
                        r.get(field(name("bucket")), String.class),
                        r.get(field(name("outgoing_request_location")), String.class),   // HDC-30: new field
                        r.get(field(name("request_processed_location")), String.class),
                        r.get(field(name("request_sent_location")), String.class)
                ));
    }
}

