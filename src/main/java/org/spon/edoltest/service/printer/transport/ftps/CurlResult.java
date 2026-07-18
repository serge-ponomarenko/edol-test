package org.spon.edoltest.service.printer.transport.ftps;

public record CurlResult(
        int exitCode,
        String stdout,
        String stderr) {
}
