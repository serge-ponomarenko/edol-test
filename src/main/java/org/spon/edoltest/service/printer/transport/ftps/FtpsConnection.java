package org.spon.edoltest.service.printer.transport.ftps;

public record FtpsConnection(
        String host,
        int port,
        String username,
        String password
) {}
