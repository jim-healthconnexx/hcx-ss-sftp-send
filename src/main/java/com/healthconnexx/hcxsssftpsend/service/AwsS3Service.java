package com.healthconnexx.hcxsssftpsend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;

/**
 * HDC-24: AWS S3 operations — download and move (copy + delete) files.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AwsS3Service {

    private final S3Client s3Client;

    /**
     * HDC-24: Download a file from S3.
     *
     * @param bucket the S3 bucket
     * @param key    the object key
     * @return an InputStream for the object content (caller must close it)
     */
    public InputStream downloadFile(String bucket, String key) {
        log.debug("HDC-24: Downloading S3 object bucket={} key={}", bucket, key);
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            log.error("HDC-24: Failed to download S3 object bucket={} key={}", bucket, key, e);
            throw e;
        }
    }

    /**
     * HDC-24: Move a file within S3 by copying to the destination then deleting the source.
     * Note: this is two operations and not atomic. If delete fails, the file exists in both
     * locations and the panel status is NOT updated — the operation is re-runnable.
     *
     * @param bucket    the S3 bucket (same bucket for source and destination)
     * @param sourceKey the source object key
     * @param destKey   the destination object key
     */
    public void moveFile(String bucket, String sourceKey, String destKey) {
        log.debug("HDC-24: Moving S3 object bucket={} from={} to={}", bucket, sourceKey, destKey);
        try {
            // Step 1: Copy to destination
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucket)
                    .destinationKey(destKey)
                    .build());
            log.debug("HDC-24: S3 copy complete bucket={} from={} to={}", bucket, sourceKey, destKey);

            // Step 2: Delete source
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(sourceKey)
                    .build());
            log.debug("HDC-24: S3 delete complete bucket={} key={}", bucket, sourceKey);

        } catch (Exception e) {
            log.error("HDC-24: Failed to move S3 object bucket={} from={} to={}", bucket, sourceKey, destKey, e);
            throw e;
        }
    }
}

