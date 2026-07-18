package org.spon.edoltest.service.printer.transport.ftps;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CurlFtpsClient {

    private final CurlExecutor executor;

    public List<String> listFiles(FtpsConnection connection) throws IOException, InterruptedException {
        CurlResult result = executor.execute(List.of(
                "curl",
                "--silent",
                "--show-error",
                "--fail",
                "--insecure",
                "--ssl-reqd",
                "--user", connection.username() + ":" + connection.password(),
                "--list-only",
                "ftps://" + connection.host() + ":" + connection.port() + "/"
        ));

        if (result.exitCode() != 0) {
            throw new IOException(result.stderr());
        }

        return result.stdout()
                .lines()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    public void download(FtpsConnection connection,
                         String remoteFile,
                         String localFile) throws IOException, InterruptedException {
        CurlResult result = executor.execute(List.of(
                "curl",
                "--silent",
                "--show-error",
                "--fail",
                "--insecure",
                "--ssl-reqd",
                "--user", connection.username() + ":" + connection.password(),
                "--output", localFile,
                "ftps://" + connection.host() + ":" + connection.port() + "/" + remoteFile
        ));

        if (result.exitCode() != 0) {
            throw new IOException(result.stderr());
        }
    }

}
