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

public class NativeWrappers {
    public static void main(final String[] args) throws IOException, InterruptedException {
        if (args.length != 8) {
            System.err.println("Usage: NativeWrappers <versionThreeDigits> <versionSuffix> <versionSha> <apiPrefix> <javaPrefix> <inputFile> <outputJava> <outputCs>");
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

        final String preprocess = callPreprocess(inputFile, apiPrefix, javaPrefix);

        final List<ApiEntry> api = collectApi(preprocess, apiPrefix);

        makeCsWrappers(outputCs, api, apiPrefix);
        makeCsVersion(outputCs, versionThreeDigits, versionSuffix, versionSha);

        final String javaPrefixJni = "Java_" + javaPrefix;
        final List<ApiEntry> javaApi = collectApi(preprocess, javaPrefixJni);
        makeJavaWrappers(outputJava, versionThreeDigits, javaApi, javaPrefixJni);
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

    private static class ApiEntry {
        public final String returnType;
        public final String name;
        public final String arguments;

        public ApiEntry(final String returnType, final String name, final String arguments) {
            this.returnType = returnType;
            this.name = name;
            this.arguments = arguments;
        }
    }

    private static List<ApiEntry> collectApi(String body, final String apiPrefix) {
        body = body
            .replaceAll("\\b__declspec\\s*\\(\\s*dllexport\\s*\\)", "")
            .replaceAll("\\b__cdecl\\b", "")
            .replaceAll("\\b__stdcall\\b", "");

        final Matcher matcher = Pattern.compile("(?<=^|[;}])\\s*([^;}]*?)\\s+(" + apiPrefix + "\\w+)\\s*\\((.*?)\\)\\s*"
            //  https://stackoverflow.com/questions/47162098/is-it-possible-to-match-nested-brackets-with-a-regex-without-using-recursion-or/47162099#47162099
            // + "(?=\\{)(?:(?=.*?\\{(?!.*?\\1)(.*\\}(?!.*\\2).*))(?=.*?\\}(?!.*?\\2)(.*)).)+?.*?(?=\\1)[^{]*(?=\\2$)"
            // https://stackoverflow.com/questions/17759004/how-to-match-string-within-parentheses-nested-in-java
            + "\\{([^{}]*|\\{([^{}]*|\\{[^{}]*\\})*\\})*\\}"
        ).matcher(body);

        final List<ApiEntry> api = new ArrayList<>();
        while (matcher.find())
            api.add(new ApiEntry(matcher.group(1).trim(), matcher.group(2).trim(), matcher.group(3).trim()));

        return api;
    }

    private static final Pattern cppArgRegEx = Pattern.compile("^(.*?)(\\w+)$");

    private static void makeCsWrappers(final String outputFile, final List<ApiEntry> api, final String apiPrefix) throws IOException {
        final int apiPrefixLength = apiPrefix.length();

        final Path outputPath = Paths.get(outputFile);

        String outputClass = outputPath.getFileName().toString();
        if (!outputClass.endsWith(".cs"))
            throw new RuntimeException("Can't determine C# the output class name for the outputFile(=" + outputFile + ").");
        else
            outputClass = outputClass.substring(0, outputClass.length() - 3);

        final String outputNamespace = outputPath.getParent().getFileName().toString();

        try (final BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write("using System;\n" +
                    "using System.Runtime.InteropServices;\n" +
                    "\n" +
                    "namespace " + outputNamespace + "\n" +
                    "{\n" +
                    "\t//Just entries\n" +
                    "\tinternal static class " + outputClass + "Import\n" +
                    "\t{\n" +
                    "\t\tinternal const string libName = \"" + apiPrefix.substring(0, apiPrefixLength - 1) + "\";\n" +
                    "\t\tinternal const CallingConvention callType = CallingConvention.Cdecl;\n"
                //+ "\t\tinternal const CharSet stringCharset = CharSet.Ansi;\n"
            );

            final StringBuilder objClassBody = new StringBuilder();
            objClassBody.append("\t\tstatic " + outputClass + "Obj()\n" +
                "\t\t{\n" +
                "\t\t\t" + outputClass + "Loader.Load();\n" +
                "\t\t}\n");

            final StringBuilder nativeClassBody = new StringBuilder();
            nativeClassBody.append("\t\tinternal static readonly " + outputClass + "Obj impl = new " + outputClass + "Obj();\n");

            for (final ApiEntry entry : api) {
                writer.write("\n\t\t[DllImport(libName, CallingConvention = callType)]\n");

                final String csRetType = cppTypeToCs(entry.returnType);
                writer.write("\t\tinternal static extern " + csRetType + " " + entry.name + "(");

                objClassBody.append("\n\t\tinternal ").append(csRetType).append(" ").append(entry.name).append("(");

                nativeClassBody.append("\n\t\tinternal static ").append(csRetType).append(" ").append(entry.name.replace(apiPrefix, "")).append("(");

                final String[] args = entry.arguments.split(",");
                final StringBuilder csArgs = new StringBuilder();
                final StringBuilder csCall = new StringBuilder();
                for (int ai = 0; ai < args.length; ++ai) {
                    if (ai > 0) {
                        csArgs.append(", ");
                        csCall.append(", ");
                    }
                    final String cppArg = args[ai].trim();
                    final Matcher cppArgMatcher = cppArgRegEx.matcher(cppArg);
                    if (!cppArgMatcher.matches())
                        throw new RuntimeException("Can't parse c++ argument(=" + cppArg + ").");
                    csArgs.append("[In] ").append(cppTypeToCs(cppArgMatcher.group(1))).append(" ").append(cppArgMatcher.group(2).trim());
                    csCall.append(cppArgMatcher.group(2).trim());
                }
                final String csArgsStr = csArgs.toString();
                final String csCallStr = csCall.toString();

                writer.write(csArgsStr);
                writer.write(");\n");

                objClassBody.append(csArgsStr).append(") =>\n\t\t\t" + outputClass + "Import.")
                    .append(entry.name).append("(").append(csCallStr).append(");\n");

                nativeClassBody.append(csArgsStr).append(") =>\n\t\t\timpl.")
                    .append(entry.name).append("(").append(csCallStr).append(");\n");
            }

            writer.write("\t}\n\n");

            writer.write("\t//Mono problem workaround\n");
            writer.write("\tinternal class " + outputClass + "Obj\n" +
                "\t{\n");
            writer.write(objClassBody.toString());
            writer.write("\t}\n\n");

            writer.write("\t//Actual API class\n");
            writer.write("\tinternal static class " + outputClass + "\n" +
                "\t{\n");
            writer.write(nativeClassBody.toString());
            writer.write("\t}\n");

            writer.write("}\n");
        }
    }

    private static String cppTypeToCs(String type) {
        type = getCppType(type);
        switch (type) {
            case "_Decimal64":
            case "decimal64":
            case "D64Bits":
            case "BID_UINT64":
                return "UInt64";
            case "int8":
            case "Int8":
                return "SByte";
            case "uint8":
            case "UInt8":
                return "Byte";
            case "int16":
            case "Int16":
                return "Int16";
            case "uint16":
            case "UInt16":
                return "UInt16";
            case "int32":
            case "Int32":
                return "Int32";
            case "uint32":
            case "UInt32":
                return "UInt32";
            case "int64":
            case "Int64":
                return "Int64";
            case "uint64":
            case "UInt64":
                return "UInt64";
            case "float":
            case "Float32":
                return "float";
            case "double":
            case "Float64":
                return "double";
            case "intBool":
                return "bool";
            default:
                throw new RuntimeException("Can't convert C++ type (='" + type + "') to Cs type.");
        }
    }

    private static void makeCsVersion(final String outputFile, final String versionThreeDigits, final String versionSuffix, final String versionSha) throws IOException {
        try (final BufferedWriter writer =
                 Files.newBufferedWriter(Paths.get(outputFile).getParent().resolve("Version.targets"),
                     StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<Project>\n" +
                "\t<PropertyGroup>\n" +
                "\t\t<Version>" + versionThreeDigits + ".0</Version>\n" +
                "\t\t<VersionSuffix>" + versionSuffix + "</VersionSuffix>\n" +
                "\t\t<VersionSha>" + versionSha + "</VersionSha>\n" +
                "\t</PropertyGroup>\n" +
                "</Project>\n");
        }
    }

    private static void makeJavaWrappers(final String outputFile, final String versionThreeDigits, final List<ApiEntry> javaApi, final String javaPrefix) throws IOException {
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

    static final String gccAttributePattern = "\\b__attribute__\\s*" +
        // https://stackoverflow.com/questions/17759004/how-to-match-string-within-parentheses-nested-in-java
        "\\(([^()]*|\\(([^()]*|\\([^()]*\\))*\\))*\\)";

    private static String getCppType(String type) {
        type = type.replaceAll(gccAttributePattern, "")
            .replaceAll("\\bconst\\b", "")
            .replace("\\bextern\\b", "");

        return type.trim();
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
