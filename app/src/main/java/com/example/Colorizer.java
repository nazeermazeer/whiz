package com.example;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Selector.SelectorParseException;


public final class Colorizer {
    // The strongest color declaration found for one element so far.
    private record Candidate(String color, boolean important, int specificity,
                             int order) { }

    public record ColorOutput(List<Rule> rules, Map<Element, String> colors) { }

    // A parsed stylesheet rule, including the information needed for CSS
    // cascade ordering.
    public record Rule(String selector, String color, boolean important,
                        int specificity, int order) { }

    // Matches a CSS color declaration in either a stylesheet rule or an
    // element's inline style attribute.
    private static final Pattern DECLARATION = Pattern.compile(
    "(?i)(?:^|;)\\s*color\\s*:\\s*([^;]+)"
    );
    private Colorizer() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static int addRules(String css, List<Rule> rules, int order) {
        // Comments cannot contain declarations and would otherwise confuse
        // the simple rule parser.
        String withoutComments = css.replaceAll("(?s)/\\*.*?\\*/", "");

        for (String block : withoutComments.split("}")) {
            // Each block is approximately "selector { declarations }".
            int openingBrace = block.indexOf('{');
            if (openingBrace < 0) {
                continue;
            }

            String selectorText = block.substring(0, openingBrace).trim();
            Matcher declaration = DECLARATION.matcher(
                block.substring(openingBrace + 1)
            );
            if (!declaration.find() || selectorText.startsWith("@")) {
                continue;
            }

            String rawColor = declaration.group(1).trim();
            // !important participates in the cascade before specificity.
            boolean important = rawColor.toLowerCase().endsWith("!important");
            if (important) {
                rawColor = rawColor.substring(0, rawColor.length() - 10).trim();
            }

            for (String selector : selectorText.split(",")) {
                selector = selector.trim();
                if (!selector.isBlank()) {
                    // CSS groups such as ".keyword, .name" become separate
                    // rules while retaining their original source order.
                    rules.add(new Rule(selector, normalizeColor(rawColor),
                            important, specificity(selector), order++));
                }
            }
        }

        return order;
    }

    private static int count(String value, String token) {
        return value.length() - value.replace(token, "").length();
    }

    private static int countPseudoClasses(String selector) {
        Matcher matcher = Pattern.compile(":(?!:)[a-zA-Z-]+").matcher(selector);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    public static ColorOutput getColorOutput(Document doc) throws Exception {
        List<Rule> rules = loadColorRules(doc);
        Map<Element, String> colors = new IdentityHashMap<>();
        return new ColorOutput(rules, colors);
    }

    private static int hex(String value) {
        // Convert a two-digit hexadecimal color component to decimal.
        return Integer.parseInt(value, 16);
    }

    private static String inlineColor(String style) {
        Matcher matcher = DECLARATION.matcher(style);
        return matcher.find() ? normalizeColor(matcher.group(1).trim()) : null;
    }

    private static List<Rule> loadColorRules(Document document)
     throws IOException {
        List<Rule> rules = new ArrayList<>();
        int order = 0;

        // Inline <style> blocks are part of the document itself.
        for (Element style : document.select("style")) {
            order = addRules(style.data(), rules, order);
        }

        // Load each linked stylesheet and continue the source-order counter so
        // later rules can override earlier rules with equal specificity.
        for (Element link : document.select("link[rel~=stylesheet][href]")) {
            String stylesheet = link.absUrl("href");
            if (!stylesheet.isBlank()) {
                order = addRules(readStylesheet(stylesheet), rules, order);
            }
        }

        return rules;
    }

    private static String normalizeColor(String color) {
        color = color.trim().toLowerCase();

        Matcher rgba = Pattern.compile(
        "rgba\\(\\s*([0-9.]+)\\s*,\\s*([0-9.]+)\\s*,\\s*([0-9.]+)\\s*,[^)]+\\)"
        ).matcher(color);
        if (rgba.matches()) {
            return "rgb("
                + rgba.group(1)
                + ", " + rgba.group(2)
                + ", " + rgba.group(3)
                + ")";
        }

        if (color.matches("#[0-9a-f]{3}")) {
            return "rgb(" + hex(color.substring(1, 2) + color.substring(1, 2))
                    + ", " + hex(color.substring(2, 3) + color.substring(2, 3))
                    + ", " + hex(color.substring(3, 4) + color.substring(3, 4))
                    + ")";
        }
        if (color.matches("#[0-9a-f]{6}")) {
            return "rgb(" + hex(color.substring(1, 3)) + ", "
                    + hex(color.substring(3, 5)) + ", "
                    + hex(color.substring(5, 7)) + ")";
        }
        return color;
    }

    private static String readStylesheet(String location) throws IOException {
        if (location.startsWith("http://") || location.startsWith("https://")) {
            // Fetch remote CSS as text instead of parsing it as HTML.
            return Jsoup.connect(location)
                .ignoreContentType(true)
                .execute()
                .body();
        }

        URI uri = URI.create(location);
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            // Query strings and fragments are valid in stylesheet URLs (for
            // example, "theme.css?v=2") but are not valid in File(URI).
            // They do not change which local file must be read.
            return Files.readString(new File(uri.getPath()).toPath());
        }

        return Files.readString(new File(location).toPath());
    }

    public static String resolveColor(Element element, List<Rule> rules,
                                        Map<Element, String> resolvedColors) {
        // A span can inherit the already-computed color of its parent, so
        // reuse that result when the same ancestor is visited repeatedly.
        String cached = resolvedColors.get(element);
        if (cached != null) {
            return cached;
        }

        Candidate best = null;

        // Inline CSS outranks normal stylesheet rules.
        String inlineColor = inlineColor(element.attr("style"));
        if (inlineColor != null) {
            best = new Candidate(inlineColor, false, 1_000, Integer.MAX_VALUE);
        }

        for (Rule rule : rules) {
            try {
                if (element.is(rule.selector())
                        && (best == null || wins(rule, best))) {
                    best = new Candidate(rule.color(), rule.important(),
                            rule.specificity(), rule.order());
                }
            } catch (SelectorParseException ignored) {
                // Ignore selectors unsupported by Jsoup instead of stopping
                // analysis of the rest of the page.
            }
        }

        String color;
        // color is inherited by default. Treat inherit and unset the same way
        // for this text-color-only analyzer.
        if (best == null || best.color().equalsIgnoreCase("inherit")
                || best.color().equalsIgnoreCase("unset")) {
            color = element.parent() instanceof Element parent
                    ? resolveColor(parent, rules, resolvedColors)
                    : "rgb(0, 0, 0)";
        } else if (best.color().equalsIgnoreCase("initial")) {
            color = "rgb(0, 0, 0)";
        } else {
            color = best.color();
        }

        resolvedColors.put(element, color);
        return color;
    }

    private static int specificity(String selector) {
        // Approximate CSS specificity as (IDs, classes/attributes/pseudo-
        // classes, elements), represented as 100/10/1 points.
        int ids = count(selector, "#");
        int classes = count(selector, ".") + count(selector, "[")
                + countPseudoClasses(selector);
        int elements = 0;

        for (String token : selector.split("\\s+|>|\\+|~")) {
            token = token.replaceAll("[#.].*", "").trim();
            if (!token.isBlank() && !token.equals("*")) {
                elements++;
            }
        }
        return ids * 100 + classes * 10 + elements;
    }

    private static boolean wins(Rule rule, Candidate current) {
        // CSS cascade priority: !important, specificity, then source order.
        if (rule.important() != current.important()) {
            return rule.important();
        }
        if (rule.specificity() != current.specificity()) {
            return rule.specificity() > current.specificity();
        }
        return rule.order() > current.order();
    }
}
