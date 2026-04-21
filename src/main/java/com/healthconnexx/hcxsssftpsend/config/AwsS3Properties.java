package com.healthconnexx.hcxsssftpsend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HDC-24: AWS S3 properties.
 * The endpoint field is optional — set only for LocalStack local development.
 * Spring Boot's relaxed binding maps AWS_S3_ENDPOINT env var → aws.s3.endpoint.
 * AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, and AWS_REGION are read directly
 * by the AWS SDK via its default credential/region provider chain.
 */
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class AwsS3Properties {
    // HDC-24: Optional endpoint override — populated from AWS_S3_ENDPOINT env var (local only)
    private String endpoint;
}

