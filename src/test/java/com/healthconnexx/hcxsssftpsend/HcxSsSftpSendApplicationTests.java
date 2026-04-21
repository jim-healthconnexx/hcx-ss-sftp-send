package com.healthconnexx.hcxsssftpsend;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.S3Client;

// HDC-27: Exclude DataSource and jOOQ auto-config so mvn package succeeds without a running PostgreSQL instance.
// This matches the pattern already established in hcx-ss-request.
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=" +
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
        "org.springframework.boot.jooq.autoconfigure.JooqAutoConfiguration"
})
class HcxSsSftpSendApplicationTests {

    // HDC-27: Provide a mock DSLContext so that CustomerReader and PanelReader can be wired
    // without a real DataSource.
    @MockitoBean
    private DSLContext dslContext;

    // HDC-27: Provide a mock S3Client to prevent the AWS SDK from attempting
    // credential and region resolution at bean-creation time (would fail in CI).
    @MockitoBean
    private S3Client s3Client;

    @Test
    void contextLoads() {
    }

}
