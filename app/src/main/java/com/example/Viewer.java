package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

            HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow("app/src/main/java/com/example/functions.html");

            Elements spans = mydoc.select("span");
            for (int i = 0; i < spans.size(); i++) {
                Element span = spans.get(i);
                Style style = getStyle(page, i);

                span.before(new TextNode("[rgb(255,0,0)]"));
                span.after(new TextNode("[/]"));   
                span.unwrap();
            }


        } catch (IOException err) {
            throw new RuntimeException(err);
        }

        return mydoc;
    }

    private static Style getStyle(HtmlPage page, int index) {
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
        return new Style(values[0], values[1], values[2]);
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
