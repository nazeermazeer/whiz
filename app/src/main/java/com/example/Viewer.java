package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

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


public class Viewer {
    public record Style(String color, String bgcolor, String display) {}
    private static volatile List<Style> cachedStyles;

    public static int getLine(String text, String search) {
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(search)) {
                return i;
            }
        }

        return -1;
    }


    public static Document getText(File html) {
        Document doc;

        try {
            doc = Jsoup.parse(html, "UTF-8");
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

    public static Document stylizeText(Document doc) {
        Document mydoc = doc;


        Elements ems = mydoc.select("em");
        for (int i = 0; i < ems.size(); i++) {
            Element em = ems.get(i);
            em.before(new TextNode("[italic]"));
            em.after(new TextNode("[/italic]"));
            em.unwrap();
        }

        Elements bs = mydoc.select("b");
        for (int i = 0; i < bs.size(); i++) {
            Element b = bs.get(i);  
            b.before(new TextNode("[bold]"));
            b.after(new TextNode("[/bold]"));   
            b.unwrap();
        }

        Elements strongs = mydoc.select("strong");
        for (int i = 0; i < strongs.size(); i++) {
            Element strong = strongs.get(i);
            strong.before(new TextNode("[bold]"));
            strong.after(new TextNode("[/bold]"));   
            strong.unwrap();
        }

        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(false);

            File file = new File("app/src/main/java/com/example/functions.html");
            HtmlPage page = webClient.getPage(file.toURI().toURL());

            Elements spans = mydoc.select("span");
            // Resolve every span in one browser-script call. Calling
            // executeJavaScript once per span reparses the script and
            // searches the whole HtmlUnit document repeatedly.
            List<Style> styles = getCachedStyles(page);

            for (int i = 0; i < spans.size(); i++) {
                Element span = spans.get(i);
                Style style = i < styles.size()
                        ? styles.get(i)
                        : new Style("rgb(0, 0, 0)", "transparent", "inline");

                span.before(new TextNode("[" + style.color + "]"));
                span.after(new TextNode("[/" + style.color + "]"));   
                span.unwrap();
            }


        } catch (IOException err) {
            throw new RuntimeException(err);
        }

        return mydoc;
    }

    private static List<Style> getCachedStyles(HtmlPage page) throws IOException {
        // The viewer currently renders functions.html every time, so its CSS
        // result is reusable. Avoid rebuilding the browser style information
        // each time a search result is displayed.
        List<Style> styles = cachedStyles;
        if (styles == null) {
            synchronized (Viewer.class) {
                styles = cachedStyles;
                if (styles == null) {
                    styles = List.copyOf(getStyles(page));
                    cachedStyles = styles;
                }
            }
        }
        return styles;
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
