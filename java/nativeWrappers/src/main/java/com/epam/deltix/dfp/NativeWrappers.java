package com.epam.deltix.dfp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.epam.deltix.dfp.ApiEntry.collectApi;

public class NativeWrappers {
    public static void main(final String[] args) throws IOException, InterruptedException {
        if (args.length != 9) {
            System.err.println("Usage: NativeWrappers <versionThreeDigits> <versionSuffix> <versionSha> <apiPrefix> <javaPrefix> <inputFile> <outputJava> <outputCs> <outputCxx> <outputC>");
            System.exit(-1);
        }
        final String versionThreeDigits = args[0];
        final String versionSuffix = args[1];
        final String versionSha = args[2];
        final String apiPrefix = args[3];
        final String javaPrefix = args[4];
        final String inputFile = args[5];
        final String outputJava = args[6];
        final String outputCs = args[7];
        final String outputCRoot = args[8];

        final String preprocess = callPreprocess(inputFile, apiPrefix, javaPrefix);

        final List<ApiEntry> api = collectApi(preprocess, apiPrefix);
        if (api.isEmpty()) {
            throw new RuntimeException("Can't collect API.");
        }

        if (!outputCRoot.isEmpty()) {
            CxxWrappers.make(outputCRoot, api, apiPrefix, versionThreeDigits);
        }

        if (!outputCs.isEmpty()) {
            CsWrappers.make(outputCs, api, apiPrefix);
            CsWrappers.makeVersion(outputCs, versionThreeDigits, versionSuffix, versionSha);
        }

        if (!outputJava.isEmpty()) {
            final String javaPrefixJni = "Java_" + javaPrefix;
            final List<ApiEntry> javaApi = collectApi(preprocess, javaPrefixJni);
            JavaWrappers.make(outputJava, versionThreeDigits, javaApi, javaPrefixJni);
        }
    }

    private static class StreamCollector implements Runnable {
        final Process process;
        private final InputStream stream;
        private final Thread thread;
        public String message;

        public StreamCollector(final Process process, final InputStream stream) {
            this.process = process;
            this.stream = stream;
            this.thread = new Thread(this);
            thread.start();
        }

        public void run() {
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                final StringBuilder sb = new StringBuilder();
                while (true) {
                    String line;
                    while ((line = reader.readLine()) != null)
                        if (!line.startsWith("#"))
                            sb.append(line);

                    if (!process.isAlive())
                        break;
                    Thread.sleep(100);
                }

                message = sb.toString();
            } catch (final IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public String getMessage() {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return message;
        }
    }

    private static String callPreprocess(final String inputFilename, final String apiPrefix, final String javaPrefix) throws IOException, InterruptedException {
        final Process process = new ProcessBuilder().command("clang", "-DAPI_PREFIX=" + apiPrefix, "-DJAVA_PREFIX=" + javaPrefix, "-E", inputFilename).start();

        final StreamCollector stdOutCollector = new StreamCollector(process, process.getInputStream());
        final StreamCollector stdErrCollector = new StreamCollector(process, process.getErrorStream());

        process.waitFor(); // Ignore exitCode because of missed headers

        return stdOutCollector.getMessage();
    }
}
