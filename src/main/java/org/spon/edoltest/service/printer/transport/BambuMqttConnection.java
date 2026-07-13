package org.spon.edoltest.service.printer.transport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.spon.edoltest.util.SslUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class BambuMqttConnection implements MqttCallback {

    private final ObjectMapper mapper = new ObjectMapper();

    @Getter
    private MqttClient client;

    @Value("${bambu.host}")
    private String bambuHost;
    @Value("${bambu.port}")
    private String bambuPort;
    @Value("${bambu.client-id}")
    private String clientId;
    @Value("${bambu.access-code}")
    private String accessCode;
    @Value("${bambu.serial}")
    private String serial;

    public synchronized void connect() {
        try {
            if (client != null && client.isConnected()) {
                return;
            }

            String connectUrl =
                    "ssl://" +
                            bambuHost + ":" + bambuPort;

            client = new MqttClient(
                    connectUrl,
                    clientId
            );

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("bblp");
            options.setPassword(accessCode.toCharArray());
            options.setAutomaticReconnect(false);
            options.setSocketFactory(SslUtil.createTrustAllSocketFactory());
            options.setKeepAliveInterval(60);

            // Disable hostname verification
            options.setHttpsHostnameVerificationEnabled(false);
            options.setConnectionTimeout(
                    5
            );  // seconds

            client.setCallback(this);

            client.connect(options);

            client.subscribe("device/" + serial + "/report");

            log.info("Connected to Bambu MQTT");

        } catch (Exception e) {
            log.error("Printer connection failed!", e);
        }

    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    @Override
    public void messageArrived(
            String topic,
            MqttMessage message
    ) {
        JsonNode root = null;
        try {
            root = mapper.readTree(message.getPayload());
        } catch (IOException e) {
            log.error("Error", e);
        }
        log.info(">>> {}", root);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Nothing
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.error("MQTT connection lost");
    }

    public void disconnect() {
        try {
            if (client != null) {
                if (client.isConnected()) {
                    client.disconnect();
                }

                client.close();
                client = null;
            }
        } catch (MqttException ignored) {

        }
    }
}
