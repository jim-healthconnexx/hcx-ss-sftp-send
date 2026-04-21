package com.healthconnexx.hcxsssftpsend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HDC-24: SureScripts SFTP connection properties.
 * Bound from surescripts.sftp.* — can be overridden via env vars
 * SURESCRIPTS_SFTP_HOST, SURESCRIPTS_SFTP_USERNAME, SURESCRIPTS_SFTP_PASSWORD, etc.
 * Real credentials to be provided once available.
 */
@ConfigurationProperties(prefix = "surescripts.sftp")
@Data
public class SftpProperties {
    // HDC-24: PLACEHOLDER values — replace with real SureScripts SFTP credentials
    private String host = "PLACEHOLDER";
    private int port = 22;
    private String username = "PLACEHOLDER";
    private String password = "PLACEHOLDER";
    private String remoteDirectory = "PLACEHOLDER";
}

