package com.healthconnexx.hcxsssftpsend.service;

import com.healthconnexx.hcxsssftpsend.config.SftpProperties;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

/**
 * HDC-24: Singleton SFTP session manager for the SureScripts SFTP server.
 *
 * Concurrency guarantee: regardless of how many threads call sendFile() concurrently,
 * only ONE SFTP connection is ever open at a time. A fair ReentrantLock serialises
 * all send operations. S3 downloads happen outside the lock.
 *
 * On SFTP failure the connection is reset so the next call reconnects cleanly.
 * On application shutdown, DisposableBean.destroy() closes the session.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SftpClientService implements DisposableBean {

    private final SftpProperties sftpProperties;

    // HDC-24: Fair lock — all callers queue in arrival order; only one holds the SFTP connection
    private final ReentrantLock sftpLock = new ReentrantLock(true);
    private Session session;
    private ChannelSftp channelSftp;

    /**
     * HDC-24: Transfer a file to the SureScripts SFTP server.
     * Acquires the lock, ensures the connection is open, performs the put, releases the lock.
     *
     * @param inputStream    the file content to send (must be open; closed by caller after return)
     * @param remoteFileName the filename to create in the remote directory
     */
    public void sendFile(InputStream inputStream, String remoteFileName) {
        sftpLock.lock();
        try {
            ensureConnected();
            String remotePath = sftpProperties.getRemoteDirectory() + "/" + remoteFileName;
            log.info("HDC-24: Sending file via SFTP remotePath={}", remotePath);
            channelSftp.put(inputStream, remotePath);
            log.info("HDC-24: SFTP send complete remotePath={}", remotePath);
        } catch (JSchException | SftpException e) {
            log.error("HDC-24: SFTP send failed for remoteFileName={}", remoteFileName, e);
            disconnect(); // reset so next caller reconnects
            throw new RuntimeException("SFTP transfer failed for " + remoteFileName, e);
        } finally {
            sftpLock.unlock();
        }
    }

    // HDC-24: Open session and channel if not already connected (called inside lock)
    private void ensureConnected() throws JSchException {
        if (session == null || !session.isConnected()) {
            log.info("HDC-24: Establishing SFTP session host={} port={}",
                    sftpProperties.getHost(), sftpProperties.getPort());
            JSch jsch = new JSch();
            session = jsch.getSession(
                    sftpProperties.getUsername(),
                    sftpProperties.getHost(),
                    sftpProperties.getPort());
            session.setPassword(sftpProperties.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            log.info("HDC-24: SFTP session connected");
        }
        if (channelSftp == null || !channelSftp.isConnected()) {
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            log.info("HDC-24: SFTP channel connected");
        }
    }

    // HDC-24: Cleanly disconnect channel and session, null both references
    private void disconnect() {
        try {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
                log.debug("HDC-24: SFTP channel disconnected");
            }
        } finally {
            channelSftp = null;
        }
        try {
            if (session != null && session.isConnected()) {
                session.disconnect();
                log.debug("HDC-24: SFTP session disconnected");
            }
        } finally {
            session = null;
        }
    }

    /** HDC-24: Clean shutdown — called by Spring on application stop. */
    @Override
    public void destroy() {
        log.info("HDC-24: Shutting down SftpClientService");
        sftpLock.lock();
        try {
            disconnect();
        } finally {
            sftpLock.unlock();
        }
    }
}

