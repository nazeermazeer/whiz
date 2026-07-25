package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlunit.ScriptResult;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import dev.tamboui.toolkit.elements.MarkupTextAreaElement;


public class Viewer {
    public record Style(String color, String bgcolor, String display) {}
    private final Map<String, Runnable> actions = new HashMap<>(); 

    public static int getLine(String text, String search) {
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(search)) {
                return i;
            }
        }

        return -1;
    }

    public static String getTitle(File html) {
        String title;
        try {
            Document doc = Jsoup.parse(html, "UTF-8", html.toURI().toString());
            title = doc.title();
        } catch (IOException err) {
            throw new RuntimeException(err);
        }

        return title;
    }


    public static Document getText(File html) {
        Document doc;

        try {
            // Preserve the file URI so Colorizer can resolve relative
            // stylesheet links such as ../_static/pygments.css.
            doc = Jsoup.parse(html, "UTF-8", html.toURI().toString());
            doc.outputSettings().prettyPrint(false);

            Elements tables = doc.select("table");

            for (Element table : tables) {
                String renderedTable = getTableText(table);
                table.replaceWith(new org.jsoup.nodes.TextNode(renderedTable));
            }

        } catch (IOException err) {
            throw new RuntimeException(err);
        } 

        return doc;
    }

    public static Document stylizeText(Document doc, File origin) {
        Elements ems = doc.select("em");
        for (Element em : ems) {
            em.before(new TextNode("[italic]"));
            em.after(new TextNode("[/]"));
            em.unwrap();
        }

        Elements bs = doc.select("b");
        for (Element b : bs) {
            b.before(new TextNode("[bold]"));
            b.after(new TextNode("[/]"));   
            b.unwrap();
        }

        Elements strongs = doc.select("strong");
        for (Element strong : strongs) {
            strong.before(new TextNode("[bold]"));
            strong.after(new TextNode("[/]"));   
            strong.unwrap();
        }

        Elements as = doc.select("a");
        for (Element a : as) {
            a.before(new TextNode("[action=" + a.attr("href") + "]"));
            a.after(new TextNode("[/action]"));   
            a.unwrap();
        }

        try {
            Colorizer.ColorOutput color = Colorizer.getColorOutput(doc);

            Elements spans = doc.select("span");
            for (Element span : spans) {
                String rgb = Colorizer.resolveColor(span, color.rules(), color.colors());
                // String rgb = "rgb(255, 0, 0)";
                span.before(new TextNode("[" + rgb + "]"));
                span.after(new TextNode("[/]"));
                span.unwrap();
            }
        } catch (Exception err) {
            throw new RuntimeException(err);
        }

        return doc;
    }

    private static List<Style> getStyles(HtmlPage page) throws IOException {
        // Run one browser script so the result includes styles from external
        // stylesheets, inherited colors, class selectors, and inline styles.
        // The result order matches Jsoup's document.select("span") order.

        String script = Files.readString(Path.of("app/src/main/java/com/example/colorizer.js"));


        ScriptResult result = page.executeJavaScript(script);
        // HtmlUnit returns the JavaScript string as one value. Split it back
        // into the three fields stored by the Style record.
        String value = String.valueOf(result.getJavaScriptResult());
        String[] values = value.split("\u001f\u001f", -1);
        List<Style> styles = new ArrayList<>(values.length);

        for (String styleValue : values) {
            String[] fields = styleValue.split("\u001f", -1);
            if (fields.length == 3) {
                styles.add(new Style(fields[0], fields[1], fields[2]));
            }
        }

        return styles;

    }
    
    private static String getTableText(Element table) {
        List<List<String>> rows = new ArrayList<>();
        int maxColumns = 0;

        for (Element row : table.select("tr")) {
            List<String> cells = new ArrayList<>();

            for (Element cell : row.select("th, td")) {
                int colspan = 1;
                String colspanValue = cell.attr("colspan");
                if (!colspanValue.isBlank()) {
                    try {
                        colspan = Integer.parseInt(colspanValue);
                    } catch (NumberFormatException ignored) {
                        colspan = 1;
                    }
                }

                String text = cell.wholeText().trim();
                for (int i = 0; i < colspan; i++) {
                    cells.add(i == 0 ? text : "");
                }
            }

            if (!cells.isEmpty()) {
                maxColumns = Math.max(maxColumns, cells.size());
                rows.add(cells);
            }
        }

        if (rows.isEmpty()) {
            return "";
        }

        for (List<String> row : rows) {
            while (row.size() < maxColumns) {
                row.add("");
            }
        }

        AsciiTable at = new AsciiTable();
        at.setTextAlignment(TextAlignment.LEFT);

        for (List<String> row : rows) {
            at.addRule();
            at.addRow(((Object[]) row.toArray(new String[0])));
        }

        at.addRule();

        return at.render();
    }

    public MarkupTextAreaElement registerActions(MarkupTextAreaElement element, Document doc) {
        Pattern pattern = Pattern.compile("\\[action=([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(doc.body().wholeText());

        while (matcher.find()) {
            String id = matcher.group(1);
            actions.put(id, () -> {
                int line;
                try {
                    line = Viewer.getLine(doc.body().wholeText(), doc.getElementById(id.replaceFirst("^#", "")).text());
                } catch (NullPointerException err) {
                    line = 0;
                }
                element.state().scrollToLine(line);
            }); 
        }

        actions.forEach(element::action);
        return element;
    }

    public static String getRubbishText() {
        SecureRandom myrandom = new SecureRandom();
        List<String> entries = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("app/src/main/java/com/example/rubbish.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                entries.add(line);
            }
        } catch (IOException err) {
            entries.add("rubbish is not rubbishing");
        }

        int randomIndex = myrandom.nextInt(entries.size());
        String line = entries.get(randomIndex);
    

        return line;
    }
}
