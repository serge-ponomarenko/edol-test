package org.spon.edoltest.service.printer.transport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spon.edoltest.service.printer.transport.ftps.CurlFtpsClient;
import org.spon.edoltest.service.printer.transport.ftps.FtpsConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BambuConnector {

    private static final UUID PRINTER_ID = UUID.randomUUID();

    private static final Path SNAPSHOT_DIR = Path.of("snapshots");

    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final BambuMqttConnection bambuMqttConnection;
    private final CameraProvider cameraProvider;
    private final CurlFtpsClient curlFtpsClient;

    @Value("${bambu.host}")
    private String bambuHost;
    @Value("${bambu.access-code}")
    private String accessCode;

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() throws IOException, InterruptedException {
        bambuMqttConnection.connect();

//        String filename = "Куб + Циліндр + Прямокутник з закругленими + Прямокутн....gcode.3mf";
//        String modelDir = "";
//        FtpsConnection ftpsConnection = new FtpsConnection(
//                bambuHost,
//                990,
//                "bblp",
//                accessCode
//        );
//        System.out.println(curlFtpsClient.listFiles(ftpsConnection));
//        curlFtpsClient.download(ftpsConnection, modelDir + "/" + filename, "snapshots/" + filename);
//        try {
//            Files.createDirectories(SNAPSHOT_DIR);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    //@Scheduled(fixedDelay = 60000)
//    public void captureSnapshot() {
//        try {
//            byte[] jpeg =
//                    cameraProvider.capture(PRINTER_ID);
//
//            Path file =
//                    SNAPSHOT_DIR.resolve(
//                            FILE_FORMAT.format(LocalDateTime.now()) + ".jpg"
//                    );
//
//            Files.write(file, jpeg);
//
//            log.info("Snapshot saved: {}", file.toAbsolutePath());
//
//        } catch (Exception e) {
//            log.error("Snapshot capture failed", e);
//        }
//    }

}