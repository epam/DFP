package com.epam.deltix.dfp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiEntry {
    public final String returnType;
    public final String name;
    public final String arguments;

    public ApiEntry(final String returnType, final String name, final String arguments) {
        this.returnType = returnType;
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return returnType + ' ' + name + '(' + arguments + ')';
    }

    public static List<ApiEntry> collectApi(String body, final String apiPrefix) {
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

    public static final Pattern cppArgRegEx = Pattern.compile("^(.*?)(\\w+)$");

    static final String gccAttributePattern = "\\b__attribute__\\s*" +
        // https://stackoverflow.com/questions/17759004/how-to-match-string-within-parentheses-nested-in-java
        "\\(([^()]*|\\(([^()]*|\\([^()]*\\))*\\))*\\)";

    public static String getCppType(final String type) {
        return getCppType(type, true);
    }

    public static String getCppType(String type, boolean replaceConst) {
        type = type.replaceAll(gccAttributePattern, "");
        if (replaceConst)
            type = type.replaceAll("\\bconst\\b", "");
        type = type.replace("\\bextern\\b", "");

        return type.trim();
    }
}
