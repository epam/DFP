package com.epam.deltix.dfp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.epam.deltix.dfp.ApiEntry.getCppType;

public class CxxWrappers {
    public static void make(final String outputRoot, final List<ApiEntry> api, final String decimalNativePrefix, final String versionThreeDigits) throws IOException {
        final String dfpType = "decimal_native_t";
        final String dfpClassType = "Decimal64_t";

        try (final BufferedWriter outFileH = Files.newBufferedWriter(Paths.get(outputRoot, "DecimalNative.h"), StandardCharsets.UTF_8);
             final BufferedWriter outFileHpp = Files.newBufferedWriter(Paths.get(outputRoot, "DecimalNative.hpp"), StandardCharsets.UTF_8)) {

            final String outputCDefine = "DECIMALNATIVE";
            final String outputCppDefine = "DECIMALNATIVEHPP";

            outFileH.write(
                "#pragma once\n" +
                    "#ifndef " + outputCDefine + "\n" +
                    "#define " + outputCDefine + "\n" +
                    "\n" +
                    "#ifdef __cplusplus\n" +
                    "#include <cstdint>\n" +
                    "#else\n" +
                    "#include <stdint.h>\n" +
                    "#endif\n" +
                    "\n" +
                    "#define " + outputCDefine + "_VERSION \"" + versionThreeDigits + "\"\n" +
                    "\n" +
                    "#ifdef __cplusplus\n" +
                    "#    define " + outputCDefine + "_MANGLING extern \"C\"\n" +
                    "#else\n" +
                    "#    define " + outputCDefine + "_MANGLING\n" +
                    "#endif\n" +
                    "\n" +
                    "#if defined(_WIN32)\n" +
                    "#    define " + outputCDefine + "_CALLING __stdcall\n" +
                    "#else\n" +
                    "#    define " + outputCDefine + "_CALLING\n" +
                    "#endif\n" +
                    "\n" +
                    "#ifdef " + outputCDefine + "_SHARED_LIBRARY\n" +
                    "#    ifdef _MSC_VER\n" +
                    "#        ifdef " + outputCDefine + "_EXPORTS\n" +
                    "#            define " + outputCDefine + "_EXPORT    __declspec(dllexport)\n" +
                    "#        else\n" +
                    "#            define " + outputCDefine + "_EXPORT    __declspec(dllimport)\n" +
                    "#        endif\n" +
                    "#    else\n" +
                    "#        define " + outputCDefine + "_EXPORT        __attribute__ ((visibility(\"default\")))\n" +
                    "#    endif\n" +
                    "#else\n" +
                    "#    define " + outputCDefine + "_EXPORT\n" +
                    "#endif\n" +
                    "\n" +
                    "#define " + outputCDefine + "_API(x) " + outputCDefine + "_MANGLING " + outputCDefine + "_EXPORT x " + outputCDefine + "_CALLING\n" +
                    "\n" +
                    "typedef struct {\n" +
                    "    uint64_t val;\n" +
                    "} " + dfpType + ";\n" +
                    "\n" +
                    "inline static uint64_t " + decimalNativePrefix + "toUnderlying(decimal_native_t _value) {\n" +
                    "    return _value.val;\n" +
                    "}\n" +
                    "\n" +
                    "inline static decimal_native_t " + decimalNativePrefix + "fromUnderlying(uint64_t value) {\n" +
                    "    decimal_native_t dn;\n" +
                    "    dn.val = value;\n" +
                    "    return dn;\n" +
                    "}\n" +
                    "\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_POSITIVE_INFINITY = 0x7800000000000000ULL;\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_NEGATIVE_INFINITY = 0xF800000000000000ULL;\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_NAN = 0x7C00000000000000ULL;\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_NULL = 0xFFFFFFFFFFFFFF80ULL;\t// = -0x80\n" +
                    "\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_MIN_VALUE = 0xF7FB86F26FC0FFFFULL;\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_MAX_VALUE = 0x77FB86F26FC0FFFFULL;\n" +
                    "\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_MIN_POSITIVE_VALUE = 0x0000000000000001ULL;\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_MAX_NEGATIVE_VALUE = 0x8000000000000001ULL;\n" +
                    "\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_ZERO = 0x31C0000000000000ULL; // e=0, m=0, sign=0\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_ONE = 0x31C0000000000001ULL;\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_TWO = 0x31C0000000000002ULL;\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_TEN = 0x31C000000000000AULL;\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_HUNDRED = 0x31C0000000000064ULL;\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_THOUSAND = 0x31C00000000003E8ULL;\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_MILLION = 0x31C00000000F4240ULL;\n" +
                    "\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_ONETENTH = 0x31A0000000000000ULL + 1;\n" +
                    "static const uint64_t DECIMAL_NATIVE_UNDERLYING_ONEHUNDREDTH = 0x3180000000000000ULL + 1;\n" +
                    "\n");

            outFileHpp.write(
                "#pragma once\n" +
                    "#ifndef " + outputCppDefine + "\n" +
                    "#define " + outputCppDefine + "\n" +
                    "\n" +
                    "#include \"DecimalNative.h\"\n" +
                    "#include <iostream>\n" +
                    "#include <string>\n" +
                    "#include <cstring>\n" +
                    "#include <stdexcept>\n" +
                    "\n" +
                    "#define DN_STRINGIFY(x) #x\n" +
                    "#define DN_TOSTRING(x) DN_STRINGIFY(x)\n" +
                    "#if defined(_MSC_VER)\n" +
                    "#define DN_FUNC (std::string(\"At \") + __FILE__ + \"[\" + DN_TOSTRING(__LINE__) + \"] \" + __FUNCSIG__)\n" +
                    "#elif defined(__GNUC__)\n" +
                    "#define DN_FUNC (std::string(\"At \") + __FILE__ + \"[\" + DN_TOSTRING(__LINE__) + \"] \" + __PRETTY_FUNCTION__)\n" +
                    "#else\n" +
                    "#define DN_FUNC (std::string(\"At \") + __FILE__ + \"[\" + DN_TOSTRING(__LINE__) + \"] \" + __func__)\n" +
                    "#endif\n" +
                    "#define DN_NAMEOF(a) #a\n" +
                    "\n" +
                    "inline std::ostream& operator <<(std::ostream& output, " + dfpType + " const& a) {\n" +
                    "    char str[512];\n" +
                    "    output << " + decimalNativePrefix + "to_string_3(a, '.', str);\n" +
                    "    return output;\n" +
                    "}\n" +
                    "\n" +
                    "inline std::istream& operator >>(std::istream& input, " + dfpType + "& a) {\n" +
                    "    std::string word;\n" +
                    "    input >> word;\n" +
                    "    a = " + decimalNativePrefix + "parse(word.c_str());\n" +
                    "    return input;\n" +
                    "}\n" +
                    "\n" +
                    "namespace epam {\n" +
                    "    namespace deltix {\n" +
                    "        namespace dfp {\n" +
                    "\n" +
                    "            template <bool nullCheck = false>\n" +
                    "            class " + dfpClassType + " {\n" +
                    "            protected:\n" +
                    "                " + dfpType + " _value;\n" +
                    "\n" +
                    "            public:\n" +
                    "                uint64_t toUnderlying() const {\n" +
                    "                    return _value.val;\n" +
                    "                }\n" +
                    "                static " + dfpClassType + " fromUnderlying(uint64_t value) {\n" +
                    "                    decimal_native_t dn;\n" +
                    "                    dn.val = value;\n" +
                    "                    return " + dfpClassType + "(dn);\n" +
                    "                }\n" +
                    "                " + dfpClassType + "() {\n" +
                    "                    _value.val = DECIMAL_NATIVE_UNDERLYING_ZERO;\n" +
                    "                }\n" +
                    "                " + dfpClassType + "(" + dfpClassType + " const &b) {\n" +
                    "                    _value = b._value;\n" +
                    "                }\n" +
                    "                " + dfpClassType + "& operator =(" + dfpClassType + " const &b) {\n" +
                    "                    _value = b._value;\n" +
                    "                    return *this;\n" +
                    "                }\n" +
                    "                " + dfpClassType + "(" + dfpType + " const &b) {\n" +
                    "                    _value = b;\n" +
                    "                }\n" +
                    "                " + dfpClassType + "& operator =(" + dfpType + " const &b) {\n" +
                    "                    _value = b;\n" +
                    "                    return *this;\n" +
                    "                }\n" +
                    "                explicit operator " + dfpType + "() const {\n" +
                    "                    return _value;\n" +
                    "                }\n" +
                    "                " + dfpClassType + " operator +(" + dfpClassType + " const& b) const {\n" +
                    "                    return add(*this, b);\n" +
                    "                }\n" +
                    "                " + dfpClassType + "& operator +=(" + dfpClassType + " const& b) {\n" +
                    "                    *this = add(*this, b);\n" +
                    "                    return *this;\n" +
                    "                }\n" +
                    "                " + dfpClassType + "& operator++() {\n" +
                    "                    *this += fromUnderlying(DECIMAL_NATIVE_UNDERLYING_ONE);\n" +
                    "                    return *this;\n" +
                    "                }\n" +
                    "                " + dfpClassType + " operator++(int) {\n" +
                    "                    " + dfpClassType + " ret = *this;\n" +
                    "                    ++*this;\n" +
                    "                    return ret;\n" +
                    "                }\n" +
                    "                " + dfpClassType + " operator -(" + dfpClassType + " const& b) const {\n" +
                    "                    return subtract(*this, b);\n" +
                    "                }\n" +
                    "                " + dfpClassType + "& operator -=(" + dfpClassType + " const& b) {\n" +
                    "                    *this = subtract(*this, b);\n" +
                    "                    return *this;\n" +
                    "                }\n" +
                    "                " + dfpClassType + "& operator--() {\n" +
                    "                    *this -= fromUnderlying(DECIMAL_NATIVE_UNDERLYING_ONE);\n" +
                    "                    return *this;\n" +
                    "                }\n" +
                    "                " + dfpClassType + " operator--(int) {\n" +
                    "                    " + dfpClassType + " ret = *this;\n" +
                    "                    --*this;\n" +
                    "                    return ret;\n" +
                    "                }\n" +
                    "                " + dfpClassType + " operator *(" + dfpClassType + " const& b) const {\n" +
                    "                    return multiply(*this, b);\n" +
                    "                }\n" +
                    "                " + dfpClassType + "& operator *=(" + dfpClassType + " const& b) {\n" +
                    "                    *this = multiply(*this, b);\n" +
                    "                    return *this;\n" +
                    "                }\n" +
                    "                " + dfpClassType + " operator /(" + dfpClassType + " const& b) const {\n" +
                    "                    return divide(*this, b);\n" +
                    "                }\n" +
                    "                " + dfpClassType + "& operator /=(" + dfpClassType + " const& b) {\n" +
                    "                    *this = divide(*this, b);\n" +
                    "                    return *this;\n" +
                    "                }\n" +
                    "                explicit operator std::string() const {\n" +
                    "                    char str[32];\n" +
                    "                    toString(str);\n" +
                    "                    return std::string(str);\n" +
                    "                }\n" +
                    "                friend std::ostream& operator <<(std::ostream& output, " + dfpClassType + " const& a) {\n" +
                    "                    output << a._value;\n" +
                    "                    return output;\n" +
                    "                }\n" +
                    "                friend std::istream& operator >>(std::istream& input, " + dfpClassType + "& a) {\n" +
                    "                    decimal_native_t val;\n" +
                    "                    input >> val;\n" +
                    "                    a = " + dfpClassType + "(val);\n" +
                    "                    return input;\n" +
                    "                }\n" +
                    "                bool isNull() const {\n" +
                    "                    return toUnderlying() == DECIMAL_NATIVE_UNDERLYING_NULL;\n" +
                    "                }\n" +
                    "\n");

            for (final ApiEntry entry : api) {
                final String fnRet = cppTypeToC(entry.returnType);
                final String fnNameC = entry.name;
                final String fnNameApi = entry.name.substring(decimalNativePrefix.length());
                String fnArg = entry.arguments;

                int fnArgCount = 0;
                for (int i = 0; i < fnArg.length(); ++i) {
                    if (fnArg.charAt(i) == ',')
                        fnArgCount++;
                }

                final StringBuilder fnCallBuilder = new StringBuilder();
                final StringBuilder fnArgBuilder = new StringBuilder();
                boolean allArgDfp = true;
                boolean anyArgDfp = false;
                for (final String oneArg : fnArg.split(",")) {
                    final SoftMatcher oneArgFinder = new SoftMatcher(Pattern.compile("^\\s*(.+?)(\\w+)\\s*$").matcher(oneArg));
                    if (oneArgFinder.matches()) {
                        if (fnCallBuilder.length() != 0) {
                            fnCallBuilder.append(", ");
                            fnArgBuilder.append(", ");
                        }
                        final String argType = cppTypeToC(oneArgFinder.group(1));
                        final String argName = oneArgFinder.group(2);
                        fnCallBuilder.append(argName);
                        if (argType.equals(dfpType)) {
                            anyArgDfp = true;
                            fnCallBuilder.append("._value");
                        } else {
                            allArgDfp = false;
                        }
                        fnArgBuilder.append(argType).append(" ").append(argName);
                    }
                }
                final String fnCall = fnCallBuilder.toString();
                fnArg = fnArgBuilder.toString();

                outFileH.write(outputCDefine + "_API(" + fnRet + ") " + fnNameC + "(" + fnArg + ");\n");

                String preOutHpp = "";
                String outHpp = "";
                if (outHpp.isEmpty() && fnArgCount == 0 && fnNameApi.startsWith("from")) {
                    outHpp += "                explicit " + dfpClassType + "(" + fnArg + ") {\n" +
                        "                    _value = " + fnNameC + "(" + fnCall + ");\n" +
                        "                }\n";
                }

                if (outHpp.isEmpty() && fnArgCount == 0 && fnNameApi.equals("parse")) {
                    outHpp += "                explicit " + dfpClassType + "(" + fnArg + ") {\n" +
                        "                    _value = " + fnNameC + "(" + fnCall + ");\n" +
                        "                }\n" +
                        "\n" +
                        "                explicit " + dfpClassType + "(const std::string &str) : " + dfpClassType + "(str.c_str()) {\n" +
                        "                }\n";
                }

                if (outHpp.isEmpty() && fnArgCount == 0 && fnArg.startsWith(dfpType) && fnNameApi.startsWith("to") && !fnNameApi.startsWith("to_scientific_string")) {
                    preOutHpp = "                explicit operator " + fnRet + "() const {\n" +
                        "                    return " + fnNameApi + "(_value);\n" +
                        "                }\n";
                    outHpp = "                " + fnRet + " " + fnNameApi + "() const {\n" +
                        "                    return " + fnNameC + "(_value);\n" +
                        "                }\n";
                }

                if (outHpp.isEmpty() && fnArgCount == 0 && fnArg.startsWith(dfpType) && (fnNameApi.startsWith("is") || fnNameApi.equals("signBit")))
                    outHpp = "                bool " + fnNameApi + "() const {\n" +
                        "                    return " + fnNameC + "(_value) != 0;\n" +
                        "                }\n";

                if (outHpp.isEmpty() && fnRet.equals(dfpType) && fnArg.startsWith(dfpType) &&
                    (fnArgCount == 0 || fnNameApi.equals("scaleByPowerOfTen"))) {
                    final SoftMatcher fnArg2Finder = new SoftMatcher(Pattern.compile(dfpType + "\\s+\\w+\\s*,?\\s*(.*)").matcher(fnArg));
                    final int fnCall2Index = fnCall.indexOf(",");
                    final String fnCall2 = fnCall2Index > 0 ? fnCall.substring(fnCall2Index).trim() : "";

                    outHpp = "                " + dfpClassType + "& " + fnNameApi + "(" + fnArg2Finder.group(1).replace(dfpType, dfpClassType + " const&") + ") {\n" +
                        "                    _value = " + fnNameC + "(_value" + fnCall2 + ");\n" +
                        "\n" +
                        "                    return *this;\n" +
                        "                }\n";
                }

                if (outHpp.isEmpty() && fnNameApi.equals("multiplyAndAdd"))
                    outHpp = "                " + dfpClassType + "& " + fnNameApi + "(" + dfpClassType + " const& a, " + dfpClassType + " const& b) {\n" +
                        "                    _value = " + fnNameC + "(a._value, b._value, _value);\n" +
                        "\n" +
                        "                    return *this;\n" +
                        "                }\n";

                if (outHpp.isEmpty() && fnArgCount == 1 && allArgDfp && fnNameApi.startsWith("is")) {
                    String opName = "";
                    switch (fnNameApi) {
                        case "isEqual":
                            opName = "==";
                            break;
                        case "isNotEqual":
                            opName = "!=";
                            break;
                        case "isLess":
                            opName = "<";
                            break;
                        case "isLessOrEqual":
                            opName = "<=";
                            break;
                        case "isGreater":
                            opName = ">";
                            break;
                        case "isGreaterOrEqual":
                            opName = ">=";
                            break;
                    }
                    if (!opName.isEmpty()) {
                        if (opName.equals("==") || opName.equals("!=")) {
                            preOutHpp = "                bool operator " + opName + "(" + dfpClassType + " const &b) const {\n" +
                                "                    if (toUnderlying() == DECIMAL_NATIVE_UNDERLYING_NULL &&\n" +
                                "                        b.toUnderlying() == DECIMAL_NATIVE_UNDERLYING_NULL)\n" +
                                "                        return " + (opName.equals("==") ? "true" : "false") + ";\n" +
                                "\n" +
                                "                    return " + fnNameC + "(_value, b._value) != 0;\n" +
                                "                }\n";

                        } else {
                            preOutHpp = "                bool operator " + opName + "(" + dfpClassType + " const &b) const {\n" +
                                "                    return " + fnNameApi + "(b);\n" +
                                "                }\n";

                            outHpp = "                bool " + fnNameApi + "(" + dfpClassType + " const &b) const {\n" +
                                "                    return " + fnNameC + "(_value, b._value) != 0;\n" +
                                "                }\n";
                        }
                    }
                }

                if (outHpp.isEmpty() && fnNameApi.equals("toString")) {
                    outHpp = "                " + fnRet + " " + fnNameApi + "(char* dst) const {\n" +
                        "                    if (dst == nullptr)\n" +
                        "                        throw std::invalid_argument(DN_FUNC + \": Argument '\" + DN_NAMEOF(dst) + \"' is nullptr.\");\n" +
                        "                    if (isNull())\n" +
                        "                        memcpy(dst, \"null\", 5 * sizeof(char));\n" +
                        "                    else\n" +
                        "                        " + fnNameC + "(_value, dst);\n" +
                        "                }\n";
                }

                if (outHpp.isEmpty() && fnNameApi.startsWith("fromFixedPoint")) {
                    outHpp = "                static " + dfpClassType + " fromFixedPoint(" + fnArg + ") {\n" +
                        "                    return " + dfpClassType + "(" + fnNameC + "(" + fnCall + "));\n" +
                        "                }\n";
                }

                if (outHpp.isEmpty() && Pattern.compile("^(max|min|add|multiply|mean)\\d+|subtract|divide").matcher(fnNameApi).matches()) {
                    final SoftMatcher multipleArgFinder = new SoftMatcher(Pattern.compile("(.+?)\\d*$").matcher(fnNameApi));
                    outHpp = "                static " + fnRet.replace(dfpType, dfpClassType) + " " + multipleArgFinder.group(1) + "(" + fnArg.replace(dfpType, dfpClassType + " const&") + ") {\n" +
                        "                    return " + dfpClassType + "(" + fnNameC + "(" + fnCall + "));\n" +
                        "                }\n";
                }

                if (outHpp.isEmpty()) {
                    final SoftMatcher mulDivFinder = new SoftMatcher(Pattern.compile("^(multiply|divide)ByInt(32|64)$").matcher(fnNameApi));
                    if (mulDivFinder.matches()) {
                        preOutHpp = "                " + dfpClassType + " operator " + (mulDivFinder.group(1).equals("multiply") ? "*" : "/") + "(int" + mulDivFinder.group(2) + "_t b) const {\n" +
                            "                    return " + fnNameApi + "(_value, b);\n" +
                            "                }\n" +
                            "\n" +
                            "                " + dfpClassType + "& operator " + (mulDivFinder.group(1).equals("multiply") ? "*" : "/") + "=(int" + mulDivFinder.group(2) + "_t b) {\n" +
                            "                    _value = " + fnNameApi + "(_value, b);\n" +
                            "\n" +
                            "                    return *this;\n" +
                            "                }\n";

                        outHpp = "                " + dfpClassType + " " + fnNameApi + "(int" + mulDivFinder.group(2) + "_t b) const {\n" +
                            "                    return " + dfpClassType + "(" + fnNameC + "(_value, b));\n" +
                            "                }\n";
                    }
                }

                if (outHpp.isEmpty() && fnArg.startsWith(dfpType) && Pattern.compile("toFixedPoint|compare").matcher(fnNameApi).matches()) {
                    final SoftMatcher fnArg2Finder = new SoftMatcher(Pattern.compile(dfpType + "\\s+\\w+\\s*,?\\s*(.*)").matcher(fnArg));
                    final int fnCall2Index = fnCall.indexOf(",");
                    final String fnCall2 = fnCall2Index > 0 ? fnCall.substring(fnCall2Index).trim() : "";

                    outHpp = "                " + fnRet.replace(dfpType, dfpClassType) + " " + fnNameApi + "(" + fnArg2Finder.group(1).replace(dfpType, dfpClassType + " const&") + ") const {\n" +
                        "                    return " + fnNameC + "(_value" + fnCall2 + ");\n" +
                        "                }\n";
                }

                if (preOutHpp.isEmpty() && outHpp.isEmpty())
                    outHpp = "                static " + fnRet.replace(dfpType, dfpClassType) + " " + fnNameApi + "(" + fnArg.replace(dfpType, dfpClassType + " const&") + ") {\n" +
                        "                    return " + fnNameC + "(" + fnCall + ");\n" +
                        "                }\n";

                //            double toFloat64() const {                //                return ddfp1x0x2xSNAPSHOT_toFloat64(_value);
                //            }

                if (anyArgDfp && !outHpp.isEmpty()) {
                    final SoftMatcher outHppFinder = new SoftMatcher(Pattern.compile("(?s)(.*?)\\s+(\\S+)\\s*\\((.*?)\\)(.*?)\\{").matcher(outHpp));

                    if (!outHppFinder.group(2).contains("toString")) {

                        String outHppCall = "";
                        for (final String oneArg : outHppFinder.group(3).split(",")) {
                            final SoftMatcher oneArgFinder = new SoftMatcher(Pattern.compile("^\\s*(.+?)(\\w+)\\s*$").matcher(oneArg));
                            if (oneArgFinder.matches()) {
                                if (!outHppCall.isEmpty())
                                    outHppCall += ", ";
                                outHppCall += oneArgFinder.group(2);
                            }
                        }

                        final SoftMatcher outHppCCallFinder = new SoftMatcher(Pattern.compile(fnNameC + "\\s*\\((.*?)\\)").matcher(outHpp));
                        String nullCheck = "";
                        for (String oneArg : outHppCCallFinder.group(1).split(",")) {
                            oneArg = oneArg.trim();
                            if (oneArg.endsWith("_value")) {
                                if (oneArg.equals("_value"))
                                    nullCheck += "                    if (isNull())\n" +
                                        "                        throw std::invalid_argument(DN_FUNC + \": This object is null.\");\n";
                                else
                                    nullCheck += "                    if (" + oneArg.substring(0, oneArg.length() - 7) + ".isNull())\n" +
                                        "                        throw std::invalid_argument(DN_FUNC + \": Argument '\" + DN_NAMEOF(" + oneArg.substring(0, oneArg.length() - 7) + ") + \"' is null.\");\n";
                            }
                        }

                        outHpp = outHppFinder.group(1) + " " + outHppFinder.group(2) + "(" + outHppFinder.group(3) + ")" + outHppFinder.group(4) + "{\n" +
                            "                    return nullCheck\n" +
                            "                        ? " + outHppFinder.group(2) + "Checked(" + outHppCall + ")\n" +
                            "                        : " + outHppFinder.group(2) + "Unchecked(" + outHppCall + ");\n" +
                            "                }\n\n" +
                            outHppFinder.group(1) + " " + outHppFinder.group(2) + "Checked(" + outHppFinder.group(3) + ")" + outHppFinder.group(4) + "{\n" +
                            nullCheck + "\n" +
                            "                    return " + outHppFinder.group(2) + "Unchecked(" + outHppCall + ");\n" +
                            "                }\n\n" +
                            outHppFinder.group(1) + " " + outHppFinder.group(2) + "Unchecked(" + outHppFinder.group(3) + ")" + outHppFinder.group(4) +
                            outHpp.substring(outHpp.indexOf("{"));
                    }
                }

                outFileHpp.append(preOutHpp.isEmpty() ? "" : "\n").append(preOutHpp).append("\n").append(outHpp).append("\n")
                    .append("//--------------------------------------------------------------------------------------------------------").append("\n");
            }

            outFileH.write("\n#endif\n");

            outFileHpp.write(
                "            };\n" +
                    "\n" +
                    "            typedef " + dfpClassType + "<> Decimal64;\n" +
                    "\n" +
                    "            static const Decimal64 D64_POSITIVE_INFINITY = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_POSITIVE_INFINITY);\n" +
                    "            static const Decimal64 D64_NEGATIVE_INFINITY = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_NEGATIVE_INFINITY);\n" +
                    "            static const Decimal64 D64_NAN = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_NAN);\n" +
                    "            static const Decimal64 D64_NULL = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_NULL); // = -0x80\n" +
                    "\n" +
                    "            static const Decimal64 D64_MIN_VALUE = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_MIN_VALUE);\n" +
                    "            static const Decimal64 D64_MAX_VALUE = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_MAX_VALUE);\n" +
                    "\n" +
                    "            static const Decimal64 D64_MIN_POSITIVE_VALUE = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_MIN_POSITIVE_VALUE);\n" +
                    "            static const Decimal64 D64_MAX_NEGATIVE_VALUE = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_MAX_NEGATIVE_VALUE);\n" +
                    "\n" +
                    "            static const Decimal64 D64_ZERO = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_ZERO); // e=0, m=0, sign=0\n" +
                    "            static const Decimal64 D64_ONE = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_ONE);\n" +
                    "            static const Decimal64 D64_TWO = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_TWO);\n" +
                    "            static const Decimal64 D64_TEN = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_TEN);\n" +
                    "            static const Decimal64 D64_HUNDRED = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_HUNDRED);\n" +
                    "            static const Decimal64 D64_THOUSAND = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_THOUSAND);\n" +
                    "            static const Decimal64 D64_MILLION = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_MILLION);\n" +
                    "\n" +
                    "            static const Decimal64 D64_ONETENTH = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_ONETENTH);\n" +
                    "            static const Decimal64 D64_ONEHUNDREDTH = Decimal64::fromUnderlying(DECIMAL_NATIVE_UNDERLYING_ONEHUNDREDTH);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "#endif\n");
        }
    }

    private static String cppTypeToC(String type) {
        type = getCppType(type, false);
        switch (type) {
            case "_Decimal64":
            case "decimal64":
            case "D64Bits":
            case "BID_UINT64":
                return "decimal_native_t";
            case "int8":
            case "Int8":
                return "int8_t";
            case "uint8":
            case "UInt8":
                return "uint8_t";
            case "int16":
            case "Int16":
                return "int16_t";
            case "uint16":
            case "UInt16":
                return "uint16_t";
            case "int32":
            case "Int32":
                return "int32_t";
            case "uint32":
            case "UInt32":
                return "uint32_t";
            case "int64":
            case "Int64":
                return "int64_t";
            case "uint64":
            case "UInt64":
                return "uint64_t";
            case "float":
            case "Float32":
                return "float";
            case "double":
            case "Float64":
                return "double";
            case "intBool":
                return "int";
            case "char":
                return "char";
            case "const char *":
            case "const char*":
                return "const char *";
            case "char *":
            case "char*":
                return "char *";
            case "uint32 *":
            case "uint32*":
                return "uint32_t *";
            default:
                throw new RuntimeException("Can't convert C++ type (='" + type + "') to C type.");
        }
    }
}
