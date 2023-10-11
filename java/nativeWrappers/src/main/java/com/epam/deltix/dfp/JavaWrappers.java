package com.epam.deltix.dfp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;

import static com.epam.deltix.dfp.ApiEntry.cppArgRegEx;
import static com.epam.deltix.dfp.ApiEntry.getCppType;

public class JavaWrappers {
    public static void make(final String outputFile, final String versionThreeDigits, final List<ApiEntry> javaApi, final String javaPrefix) throws IOException {
        final int prefixLength = javaPrefix.length();

        final Path outputPath = Paths.get(outputFile);
        Files.createDirectories(outputPath.getParent());

        String outputClass = outputPath.getFileName().toString();
        if (!outputClass.endsWith(".java"))
            throw new RuntimeException("Can't determine Java the output class name for the outputFile(=" + outputFile + ").");
        else
            outputClass = outputClass.substring(0, outputClass.length() - 5);

        String outputNamespace = outputPath.getParent().toString().replace(File.separatorChar,'.');
        String generatorPackage = NativeWrappers.class.getPackage().getName();
        while(true) {
            final int namespaceIndex = outputNamespace.lastIndexOf(generatorPackage);
            if (namespaceIndex >= 0) {
                outputNamespace = outputNamespace.substring(namespaceIndex);
                break;
            }
            final int cutPoint = generatorPackage.lastIndexOf('.');
            if (cutPoint <= 0)
                throw new RuntimeException("Can't guess the output namespace for the outputFile(=" + outputFile + ").");
            generatorPackage = generatorPackage.substring(0, cutPoint);
            if (generatorPackage.lastIndexOf('.') < 0)
                throw new RuntimeException("Can't guess the output namespace for the outputFile(=" + outputFile + "): the guess namespace in too short.");
        }

        try (final BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write("package " + outputNamespace + ";\n" +
                "\n" +
                "final class " + outputClass + " {\n" +
                "    static {\n" +
                "        " + outputClass + "Loader.load();\n" +
                "    }\n" +
                "\n" +
                "    public static final String version = \"" + versionThreeDigits + "\";\n"
            );

            for (final ApiEntry entry : javaApi) {
                if (entry.name.startsWith(javaPrefix + "tryParse") ||
                    entry.name.startsWith(javaPrefix + "parse") ||
                    entry.name.startsWith(javaPrefix + "to_string") ||
                    entry.name.startsWith(javaPrefix + "to_scientific_string"))
                    continue;

                writer.write("\n    public static native " + cppTypeToJava(entry.returnType) + " " + entry.name.substring(prefixLength) + "(");

                final String[] args = entry.arguments.split(",");
                for (int ai = 2; ai < args.length; ++ai) { // Skip "void *javaEnv, void *jClass" arguments
                    if (ai > 2)
                        writer.write(", ");
                    final String cppArg = args[ai].trim();
                    final Matcher cppArgMatcher = cppArgRegEx.matcher(cppArg);
                    if (!cppArgMatcher.matches())
                        throw new RuntimeException("Can't parse c++ argument(=" + cppArg + ").");
                    writer.write(cppTypeToJava(cppArgMatcher.group(1)) + " " + cppArgMatcher.group(2).trim());
                }
                writer.write(");\n");
            }
            writer.write("}\n");
        }
    }

    private static String cppTypeToJava(String type) {
        type = getCppType(type);
        switch (type) {
            case "_Decimal64":
            case "decimal64":
            case "D64Bits":
            case "BID_UINT64":
                return "long";
            case "int8":
            case "uint8":
            case "Int8":
            case "UInt8":
                return "byte";
            case "int16":
            case "uint16":
            case "Int16":
            case "UInt16":
                return "short";
            case "int32":
            case "uint32":
            case "Int32":
            case "UInt32":
                return "int";
            case "int64":
            case "uint64":
            case "Int64":
            case "UInt64":
                return "long";
            case "float":
            case "Float32":
                return "float";
            case "double":
            case "Float64":
                return "double";
            case "intBool":
                return "boolean";
            default:
                throw new RuntimeException("Can't convert C++ type (='" + type + "') to Java type.");
        }
    }
}
