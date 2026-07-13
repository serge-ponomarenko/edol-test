package org.spon.edoltest.service.printer.transport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BambuConnector {

    private static final UUID PRINTER_ID = UUID.fromString("PUT-YOUR-PRINTER-ID-HERE");

    private static final Path SNAPSHOT_DIR = Path.of("snapshots");

    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final BambuMqttConnection bambuMqttConnection;
    private final CameraProvider cameraProvider;

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        bambuMqttConnection.connect();

        try {
            Files.createDirectories(SNAPSHOT_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedDelay = 15000)
    public void captureSnapshot() {
        try {
            byte[] jpeg =
                    cameraProvider.capture(PRINTER_ID);

            Path file =
                    SNAPSHOT_DIR.resolve(
                            FILE_FORMAT.format(LocalDateTime.now()) + ".jpg"
                    );

            Files.write(file, jpeg);

            log.info("Snapshot saved: {}", file.toAbsolutePath());

        } catch (Exception e) {
            log.error("Snapshot capture failed", e);
        }
    }

}