package org.spon.edoltest.service.printer.transport;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BambuConnector {

    private final BambuMqttConnection bambuMqttConnection;

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        bambuMqttConnection.connect();
    }


}
