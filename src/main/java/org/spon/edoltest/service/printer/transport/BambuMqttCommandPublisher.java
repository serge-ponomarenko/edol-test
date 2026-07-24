package org.spon.edoltest.service.printer.transport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BambuMqttCommandPublisher {

    private final BambuMqttConnection bambuMqttClient;

    @Value("${bambu.serial}")
    private String serial;

    public void publish(String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(0);

            bambuMqttClient.getClient().publish(
                    "device/" + serial + "/request",
                    message
            );

        } catch (MqttException e) {
            log.error("Error: {}", e);
        }
    }
}
