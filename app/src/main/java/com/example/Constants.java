package com.example;

import java.util.regex.Pattern;

public class Constants {
    public class ColorizerConstants {
        // Matches a CSS color declaration in either a stylesheet rule or an
        // element's inline style attribute.
        public static final Pattern CSSDeclaration = Pattern.compile("(?i)(?:^|;)\\s*color\\s*:\\s*([^;]+)");
    }
}
