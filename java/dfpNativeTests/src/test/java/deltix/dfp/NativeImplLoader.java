package deltix.dfp;

import com.epam.deltix.dfp.NativeImpl;
import com.epam.deltix.utilities.*;

class NativeImplLoader {
    public static void load() {
        try {
            String version = NativeImpl.version;
            final boolean isSnapshot = version.endsWith("-SNAPSHOT");
            if (isSnapshot)
                version = version.substring(0, version.length() - "-SNAPSHOT".length());

            VariablesMapper varMapper = new VariablesMapper(NativeImplLoader.class, version);

            final String unpackEnvVarName = varMapper.getPackageLast().toUpperCase() + "_UNPACK_ROOT";
            String unpackPath = System.getenv(unpackEnvVarName);
            if (unpackPath == null)
                unpackPath = "$(TEMP)/$(PACKAGE)/$(VERSION)/$(ARCH)";

            ResourceLoader
                .from(NativeImplLoader.class, "resources_" + varMapper.getPackage().replace('.', '_') + '/' + varMapper.getOs() + '/' + varMapper.getArch() + "/*")
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
