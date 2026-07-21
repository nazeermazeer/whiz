package com.example;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.htmlunit.ScriptResult;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Test {

    /**
     * Prints the browser-computed color for every span in an HTML document.
     *
     * Run com.example.Test from an IDE or Java launcher with a file path or URL
     * argument. If no argument is supplied, functions.html is analyzed.
     */
    public static void main(String[] args) throws Exception {
        // HtmlUnit does not need a graphical desktop. These properties keep
        // Java in headless mode and tell macOS not to show a Dock icon for
        // this command-line application. They must be set before HtmlUnit or
        // any AWT-dependent class is initialized.
        System.setProperty("java.awt.headless", "true");
        System.setProperty("apple.awt.UIElement", "true");

        // Use the first command-line argument as the input document. This can
        // be a file path, an HTTP(S) URL, or an HTML string beginning with '<'.
        String source = args.length == 0
                ? "app/src/main/java/com/example/functions.html"
                : args[0];

        // HtmlUnit provides a browser-like environment that can load CSS and
        // calculate the final style after inheritance and CSS selectors apply.
        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(false);

            HtmlPage page;
            Document document;

            if (source.startsWith("<")) {
                // Parse raw HTML directly with both libraries.
                document = Jsoup.parse(source);
                page = webClient.loadHtmlCodeIntoCurrentWindow(source);
            } else if (source.startsWith("http://") || source.startsWith("https://")) {
                // Let HtmlUnit load the page and its stylesheets. Jsoup then
                // analyzes the same response to find the span elements.
                page = webClient.getPage(source);
                document = Jsoup.parse(page.getWebResponse().getContentAsString(), source);
            } else {
                // A file URL preserves the file's location, allowing HtmlUnit
                // to resolve relative CSS references correctly.
                File file = new File(source).getCanonicalFile();
                document = Jsoup.parse(file, StandardCharsets.UTF_8.name());
                page = webClient.getPage(file.toURI().toURL());
            }

            // Jsoup is used for simple, reliable DOM selection and readable
            // element text/class/id information.
            var spans = document.select("span");
            System.out.println("Found " + spans.size() + " span tag(s)");

            for (int i = 0; i < spans.size(); i++) {
                Element span = spans.get(i);
                // The index keeps the Jsoup span aligned with the same span in
                // HtmlUnit's document.querySelectorAll('span') result.
                String[] computed = computedStyle(page, i);

                System.out.printf(
                        "span[%d] %s text=%s -> color=%s, background-color=%s, display=%s%n",
                        i, // span[3515]
                        describe(span), // <span.n>
                        quote(span.text()), // text="_temp"
                        computed[0], // color = rgb
                        computed[1],
                        computed[2]);
            }
        }
    }

    private static String[] computedStyle(HtmlPage page, int index) {
        // Run browser JavaScript so the result includes styles from external
        // stylesheets, inherited colors, class selectors, and inline styles.
        String script = """
                (() => {
                    const span = document.querySelectorAll('span')[%d];
                    if (!span) return 'unknown|unknown|unknown';

                    // HtmlUnit can leave an explicitly inherited color as the
                    // string "inherit". Walk up the DOM until the inherited
                    // color is resolved to an actual RGB/RGBA value.
                    function resolvedColor(element) {
                        let current = element;
                        while (current) {
                            const color = window.getComputedStyle(current).color;
                            if (color && color !== 'inherit') return color;
                            current = current.parentElement;
                        }

                        // The browser default text color is black when no
                        // ancestor supplies a color.
                        return 'rgb(0, 0, 0)';
                    }

                    const style = window.getComputedStyle(span);
                    return resolvedColor(span) + '|' + style.backgroundColor + '|' + style.display;
                })()
                """.formatted(index);

        ScriptResult result = page.executeJavaScript(script);
        // Return the three values as one string to avoid having to convert a
        // JavaScript array/object returned by HtmlUnit.
        String value = String.valueOf(result.getJavaScriptResult());
        String[] values = value.split("\\|", -1);
        return values.length == 3 ? values : new String[] { value, "unknown", "unknown" };
    }

    private static String describe(Element span) {
        // Build a compact selector-like label, such as <span#title.highlight>.
        String id = span.id().isBlank() ? "" : "#" + span.id();
        String classes = span.classNames().isEmpty() ? "" : "." + String.join(".", span.classNames());
        return "<span" + id + classes + ">";
    }

    private static String quote(String text) {
        // Quote text in the output and escape characters that would break it.
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
