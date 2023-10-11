package com.epam.deltix.dfp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.epam.deltix.dfp.ApiEntry.cppArgRegEx;
import static com.epam.deltix.dfp.ApiEntry.getCppType;

public class CsWrappers {
    public static void make(final String outputFile, final List<ApiEntry> api, final String apiPrefix) throws IOException {
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
                if (entry.name.startsWith(apiPrefix + "to_string_3") ||
                    entry.name.startsWith(apiPrefix + "to_scientific_string_3"))
                    continue;

                writer.write("\n\t\t[DllImport(libName, CallingConvention = callType, CharSet = CharSet.Ansi)]\n");

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

                    final String csType = cppTypeToCs(cppArgMatcher.group(1));
                    final boolean isOutType = csType.startsWith("out ");

                    csArgs.append(isOutType ? "[Out] " : "[In] ")
                        .append(cppTypeToCs(cppArgMatcher.group(1)))
                        .append(" ")
                        .append(cppArgMatcher.group(2).trim());
                    csCall.append(isOutType ? "out " : "")
                        .append(cppArgMatcher.group(2).trim());
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
            case "char *":
            case "char*":
                return "string";
            case "char":
                return "char";
            case "uint32 *":
            case "uint32*":
                return "out uint";
            default:
                throw new RuntimeException("Can't convert C++ type (='" + type + "') to Cs type.");
        }
    }

    public static void makeVersion(final String outputFile, final String versionThreeDigits, final String versionSuffix, final String versionSha) throws IOException {
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
}
