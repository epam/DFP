package com.epam.deltix.dfp;

import com.epam.deltix.utilities.ResourceLoader;

import java.nio.file.Paths;
import java.util.Locale;

class NativeImplLoader {
    public static void load() {
        try {
            String version = NativeImpl.version;

            final boolean isSnapshot = version.endsWith("-SNAPSHOT");
            if (isSnapshot)
                version = version.substring(0, version.length() - "-SNAPSHOT".length());

            String osName = System.getProperty("os.name");
            String osArch = System.getProperty("os.arch");

            if (osName.toLowerCase(Locale.ROOT).contains("windows"))
                osName = "Windows";
            if (osName.toLowerCase(Locale.ROOT).contains("mac")) {
                osName = "Darwin";
                if (osArch.toLowerCase(Locale.ROOT).contains("arm64"))
                    osArch = "aarch64";
            }

            ResourceLoader
                .from(NativeImpl.class, osName + "/" + osArch + "/*") // This version now also works, but is probably less efficient
                .to(Paths.get(System.getProperty("java.io.tmpdir"), "com", "EPAM", "Deltix", "DFP", version, osArch).toString())
                .alwaysOverwrite(isSnapshot)
                .tryRandomFallbackSubDirectory(true)
                .load();
        } catch (final Throwable exception) {
            //exception.printStackTrace();
            throw new RuntimeException(exception);
        }

        NativeImpl.init(0);
    }
}
