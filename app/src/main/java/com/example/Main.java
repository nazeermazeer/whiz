package com.example;

import static dev.tamboui.toolkit.Toolkit.*;

import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.example.Indexer.SearchResult;

import dev.tamboui.toolkit.elements.MarkupTextAreaElement;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.common.ScrollBarPolicy;

import dev.tamboui.widgets.input.TextInputState;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.security.SecureRandom;
import java.util.List;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

public class Main extends ToolkitApp {
    private static final Path PATH = Path.of("app/src/main/java/com/example/functions.html");
    private String TEXT = getText();
    private final TextInputState searchState = new TextInputState();

    private Indexer myindexer = new Indexer();


    @Override
    protected Element render() {
        return panel(
            PATH.getFileName().toString(),
            panel(
                markupTextArea(TEXT)
                    .wrapWord()
                    .scrollbar(ScrollBarPolicy.AS_NEEDED)
                    .borderType(BorderType.NONE)
            ),
            panel(searchbar)
        ).borderType(BorderType.NONE);
    }

    private final MarkupTextAreaElement document = markupTextArea(TEXT)
            .wrapWord()
            .scrollbar(ScrollBarPolicy.AS_NEEDED)
            .borderType(BorderType.NONE)
            .id("document")
            .focusable();

    private final Element searchbar = 
            textInput(searchState)
                .placeholder(this.getRubbishText() + "...")
                .onSubmit(() -> {
                    String input = searchState.text();
                    TEXT = "";
                    try {
                        List<SearchResult> results = myindexer.searchTerm(input);
                        for (SearchResult result : results) {
                            TEXT += "Term: " + result.term() + "Definition: " + result.definition();
                        }
                    } catch (Exception err) {
                        err.printStackTrace();
                    }

                });


    public static String getText() {
        File html = new File("app/src/main/java/com/example/functions.html");
        String text;

        try {
            Document doc = Jsoup.parse(html, "UTF-8");
            doc.outputSettings().prettyPrint(false);

            Elements tables = doc.select("table");

            for (org.jsoup.nodes.Element table : tables) {
                String renderedTable = getTableText(table);
                table.replaceWith(new org.jsoup.nodes.TextNode(renderedTable));
            }


            text = doc.body().wholeText();
        } catch (IOException err) {
            text = "could not read file";
        } 

        return text;
    }

    public static String getTableText(org.jsoup.nodes.Element table) {
        List<List<String>> rows = new ArrayList<>();
        int maxColumns = 0;

        for (org.jsoup.nodes.Element row : table.select("tr")) {
            List<String> cells = new ArrayList<>();

            for (org.jsoup.nodes.Element cell : row.select("th, td")) {
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

    public String getRubbishText() {
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


    public static void main(String[] args) throws Exception {
        new Main().run();
    }
}

