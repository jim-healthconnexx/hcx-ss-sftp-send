package com.healthconnexx.hcxsssftpsend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

/**
 * HDC-24: Spring configuration for S3Client and property bindings.
 */
@Configuration
@EnableConfigurationProperties({SftpProperties.class, AwsS3Properties.class})
@RequiredArgsConstructor
@Slf4j
public class SftpSendConfig {

    private final AwsS3Properties awsS3Properties;

    /**
     * HDC-24: Build an S3Client.
     * When aws.s3.endpoint is set (local dev with LocalStack), the endpoint is overridden
     * and path-style access is forced. For QA and prod, the AWS SDK default resolution is used.
     * HDC-29: Credentials are resolved from standard AWS env vars (AWS_ACCESS_KEY_ID,
     * AWS_SECRET_ACCESS_KEY) by the SDK DefaultCredentialsProvider chain — consistent with the
     * cross-project pattern established in HDC-11. The app must be started via docker-compose so
     * that .env.local is applied; running directly without those env vars exported will fail.
     */
    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder();

        if (awsS3Properties.getEndpoint() != null && !awsS3Properties.getEndpoint().isBlank()) {
            log.info("HDC-24: Using custom S3 endpoint (LocalStack): {}", awsS3Properties.getEndpoint()); // HDC-29: retained
            // HDC-29: LocalStack mode active. Credentials are resolved from standard AWS env vars
            // (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY) by the SDK DefaultCredentialsProvider chain.
            // If those vars are not set in the runtime environment, S3 operations will fail at call time.
            // Ensure the app is started via docker-compose so .env.local is loaded.
            log.info("HDC-29: LocalStack S3 endpoint active: {}. Credentials expected from AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY env vars.", awsS3Properties.getEndpoint());
            builder.endpointOverride(URI.create(awsS3Properties.getEndpoint()))
                    .region(Region.of("us-east-1")) // Required when endpoint is overridden
                    .forcePathStyle(true);           // Required for LocalStack
        }

        return builder.build();
    }
}

