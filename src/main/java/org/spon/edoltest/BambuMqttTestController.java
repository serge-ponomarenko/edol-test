package org.spon.edoltest;

import lombok.RequiredArgsConstructor;
import org.spon.edoltest.service.printer.transport.BambuMqttCommandPublisher;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class BambuMqttTestController {

    private final BambuMqttCommandPublisher publisher;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String page() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Bambu MQTT Test</title>
                </head>
                <body>
                    <h2>Bambu MQTT Command</h2>

                    <form method="post" action="">
                        <textarea
                            name="payload"
                            rows="20"
                            cols="100"
                            placeholder="Enter MQTT payload..."
                        >
                            {
                                "print": {
                                    "command": "ams_filament_setting",
                                    "ams_id": 0,
                                    "tray_id": 0,
                                    "tray_info_idx": "GFL99",
                                    "tray_color": "F72323FF",
                                    "nozzle_temp_min": 0,
                                    "nozzle_temp_max": 0,
                                    "tray_type": "PLA"
                                }
                            }
                        </textarea>

                        <br><br>

                        <button type="submit">Publish</button>
                    </form>
                </body>
                </html>
                """;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE
    )
    public String publish(@RequestParam String payload) {
        publisher.publish(payload);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Bambu MQTT Test</title>
                </head>
                <body>
                    <h2>Published successfully</h2>

                    <pre>%s</pre>

                    <a href="/test/mqtt">Back</a>
                </body>
                </html>
                """.formatted(escapeHtml(payload));
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}