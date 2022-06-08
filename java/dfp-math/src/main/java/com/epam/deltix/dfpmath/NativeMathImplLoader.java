package com.epam.deltix.dfpmath;

import com.epam.deltix.utilities.*;

class NativeMathImplLoader {
    public static void load() {
        try {
            String version = NativeMathImpl.version;
            final boolean isSnapshot = version.endsWith("-SNAPSHOT");
            if (isSnapshot)
                version = version.substring(0, version.length() - "-SNAPSHOT".length());

            VariablesMapper varMapper = new VariablesMapper(NativeMathImplLoader.class, version);

            final String unpackEnvVarName = varMapper.getPackageLast().toUpperCase() + "_UNPACK_ROOT";
            String unpackPath = System.getenv(unpackEnvVarName);
            if (unpackPath == null)
                unpackPath = "$(TEMP)/$(PACKAGE)/$(VERSION)/$(ARCH)";

            ResourceLoader
                .from(NativeMathImplLoader.class, "resources_" + varMapper.getPackage().replace('.', '_') + '/' + varMapper.getOs() + '/' + varMapper.getArch() + "/*")
                .to(varMapper.substitute(unpackPath))
                .alwaysOverwrite(isSnapshot)
                .tryRandomFallbackSubDirectory(true)
                .load();
        } catch (final Throwable exception) {
            //exception.printStackTrace();
            throw new RuntimeException(exception);
        }
    }
}
