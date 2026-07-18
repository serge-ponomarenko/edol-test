package org.spon.edoltest.service.printer.transport;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Setter
public class FtpsService {

    @Value("${bambu.host}")
    private String bambuHost;
    @Value("${bambu.access-code}")
    private String accessCode;


    public void download(
            String requestedFile,
            String localFile
    ) {
        int maxAttempts = 5;
        long delayMs = 5000;

        Exception lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            FTPSClient ftps = null;

            try {
                ftps = getFtpsClient(bambuHost, 990, accessCode);


                ftps.addProtocolCommandListener(new ProtocolCommandListener() {

                    @Override
                    public void protocolCommandSent(ProtocolCommandEvent event) {
                        log.info("FTP >>> {}", event.getMessage().trim());
                    }

                    @Override
                    public void protocolReplyReceived(ProtocolCommandEvent event) {
                        log.info("FTP <<< {}", event.getMessage().trim());
                    }
                });

                log.info(
                                "Downloading '{}'. Attempt: {}",
                                requestedFile,
                                attempt
                        );

                downloadModel(ftps, requestedFile, localFile, "");

                log.info(
                                "Model downloaded"
                        );

                safeLogout(ftps);
                safeDisconnect(ftps);

                return;

            } catch (IOException e) {

                lastError = e;

                        log.error(
                                "FTPS attempt {} failed: {}", attempt, e.getMessage()
                        );

                safeDisconnect(ftps);

                try {
                    Thread.sleep(delayMs * attempt);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("FTPS download interrupted", interruptedException);
                }
            }
        }

        throw new RuntimeException("Download failed after retries", lastError);
    }

    private FTPSClient getFtpsClient(String host, int port, String accessCode) throws IOException {
        FTPSClient ftps = new FTPSClient(true);

        ftps.setControlEncoding("UTF-8");

        ftps.setConnectTimeout(5000);
        ftps.setDataTimeout(Duration.ofSeconds(30));

        // keep alive
        ftps.setControlKeepAliveTimeout(Duration.ofSeconds(10));
        ftps.setControlKeepAliveReplyTimeout(Duration.ofSeconds(5));

        ftps.setStrictReplyParsing(false);
        ftps.setRemoteVerificationEnabled(false);

        ftps.connect(host, port);

        if (!ftps.login("bblp", accessCode))
            throw new RuntimeException("FTP login failed");

        ftps.setSoTimeout(30000);

        ftps.execPBSZ(0);
        log.info(
                "Reply: {} {}",
                ftps.getReplyCode(),
                ftps.getReplyString().trim()
        );

        ftps.execPROT("P");
        log.info(
                "Reply: {} {}",
                ftps.getReplyCode(),
                ftps.getReplyString().trim()
        );

        return ftps;
    }

    private void downloadModel(
            FTPSClient ftps,
            String requestedFile,
            String localFile,
            String modelDirectory
    ) throws IOException {
        ftps.setFileType(FTP.BINARY_FILE_TYPE);
        log.info(
                "Reply: {} {}",
                ftps.getReplyCode(),
                ftps.getReplyString().trim()
        );

        ftps.enterLocalPassiveMode();
        log.info(
                "Reply: {} {}",
                ftps.getReplyCode(),
                ftps.getReplyString().trim()
        );

        boolean cwd = ftps.changeWorkingDirectory(modelDirectory);

        log.info(
                "CWD '{}': success={}, reply={} {}",
                modelDirectory,
                cwd,
                ftps.getReplyCode(),
                ftps.getReplyString().trim()
        );

        String remoteFile = resolveRemoteFile(ftps, requestedFile);

        try (OutputStream output = new FileOutputStream(localFile)) {
            boolean success = ftps.retrieveFile(remoteFile, output);
            if (!success) {
                throw new RuntimeException("Download failed: " + remoteFile);
            }
        }

        try (InputStream in = new FileInputStream(localFile)) {
            byte[] header = new byte[2];
            if (in.read(header) != 2 || header[0] != 'P' || header[1] != 'K') {
                throw new RuntimeException("Invalid ZIP file: " + remoteFile);
            }
        }
    }

    private String resolveRemoteFile(FTPSClient ftps, String requested) throws IOException {
        FTPFile[] files = ftps.listFiles();
        log.info(
                "LIST returned {} entries",
                files == null ? "null" : files.length
        );

        String[] names = ftps.listNames();
        log.info(
                "LIST NAMES: {}",
                Arrays.toString(names)
        );

        FTPFile[] mlFiles = ftps.mlistDir();
        log.info(
                "MLSD returned {}",
                mlFiles == null ? "null" : mlFiles.length
        );

        for (FTPFile file : files) {
            log.info(
                    "FILE name='{}' type={} size={} raw='{}'",
                    file.getName(),
                    file.getType(),
                    file.getSize(),
                    file
            );
        }

        if (files == null || files.length == 0) {
            throw new RuntimeException("No files found on printer");
        }

        String normalizedRequested = normalizeName(requested);

        for (FTPFile file : files) {
            String name = file.getName();

            if (normalizeName(name).equalsIgnoreCase(normalizedRequested)) {
                if (file.getSize() == 0) {
                    throw new RuntimeException("Remote file is empty");
                }
                return name;
            }

        }

        throw new RuntimeException("No matching model file found on printer");
    }

    private String normalizeName(String name) {
        if (name == null)
            return "";
        return Normalizer.normalize(name, Normalizer.Form.NFKC);
    }

    private void safeLogout(FTPSClient ftps) {
        if (ftps == null) {
            return;
        }
        try {
            ftps.logout();
        } catch (IOException ignored) {
            //Nothing
        }
    }

    private void safeDisconnect(FTPSClient ftps) {
        if (ftps == null || !ftps.isConnected()) {
            return;
        }
        try {
            ftps.disconnect();
        } catch (IOException ignored) {
            //Nothing
        }
    }
}
