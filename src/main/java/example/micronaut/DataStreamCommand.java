package example.micronaut;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.reactor.http.client.ReactorStreamingHttpClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

@Command(name = "downloadfile", description = "...",
        mixinStandardHelpOptions = true)
public class DataStreamCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "Whether you want a verbose output", defaultValue = StringUtils.TRUE)
    boolean verbose;

    @Option(names = {"--url"}, required = true, defaultValue = "https://images.sergiodelamo.com/avatar.png")
    String url;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(DataStreamCommand.class, args);
    }

    public void run() {
        try {
            if (verbose) {
                System.out.println("Downloading " + url);
            }
            URL baseURL = new URL(url);
            HttpRequest<?> request = HttpRequest.GET(url);

            File outputFile = new File("downloadedImageWithStreamingHttpClientDataStream.png");
            outputFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            dataStreamToOutputStream(baseURL, request, outputStream);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dataStreamToOutputStream(URL baseURL, HttpRequest<?> request, FileOutputStream fileOutputStream) throws IOException {
        ReactorStreamingHttpClient reactorStreamingHttpClient = ReactorStreamingHttpClient.create(baseURL);
        reactorStreamingHttpClient.dataStream(request)
                .doOnNext(byteBuffer -> {
                    if (verbose) {
                        System.out.println("Saving byte array");
                    }
                    try {
                        fileOutputStream.write(byteBuffer.toByteArray());
                    } catch (IOException e) {
                        System.out.println("IO Exception" + e.getMessage());
                    }
                })
                .doFinally(signalType -> {
                    try {
                        if (verbose) {
                            System.out.println("Closing OutputStream");
                        }
                        fileOutputStream.close();
                    } catch (IOException e) {
                        System.out.println("IO Exception closing the stream" + e.getMessage());
                    }
                })
                .subscribe();
    }
}
