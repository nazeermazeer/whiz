package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;


public class Viewer {
    public static int getLine(String text, String search) {
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(search)) {
                return i;
            }
        }

        return -1;
    }


    public static String getText(File html) {
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
            throw new RuntimeException(err);
        } 

        return text;
    }

    private static String getTableText(org.jsoup.nodes.Element table) {
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
