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

import dev.tamboui.toolkit.elements.MarkupTextAreaElement;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.common.ScrollBarPolicy;

import dev.tamboui.widgets.input.TextInputState;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.security.SecureRandom;
import java.util.List;


public class Main extends ToolkitApp {
    private static final Path PATH = Path.of("app/src/main/java/com/example/functions.html");
    private static final String TEXT = getText();
    private final TextInputState searchState = new TextInputState();


    @Override
    protected Element render() {
        return panel(PATH.getFileName().toString(), panel(document).borderType(BorderType.NONE), panel(searchbar)).borderType(BorderType.NONE);
    }

    private final MarkupTextAreaElement document = markupTextArea(TEXT)
            .wrapWord()
            .scrollbar(ScrollBarPolicy.AS_NEEDED)
            .borderType(BorderType.NONE)
            .id("document")
            .focusable();

    private final Element searchbar = 
            textInput(searchState)
                .placeholder(this.getRubbishText() + "...");


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
        Elements labels = table.select("tr");
        String[] headers = new String[labels.size()];
        int numheader = 0;

        for (org.jsoup.nodes.Element label : labels) {
            String header = label.wholeText();
            headers[numheader] = header;
            numheader++;
        }



        Elements rows = table.select("tbody > tr");
        List<List<String>> rowitems = new ArrayList<>();
        String rowstr = "| ";
        int[] columnlens = new int[headers.length];
        String borders = "";

        for (int i = 0; i < columnlens.length; i++) {
            columnlens[i] = headers[i].length();
        }

        for (org.jsoup.nodes.Element row : rows) {
            Elements cells = row.select("td");

            if (columnlens == null) {
                columnlens = new int[cells.size()];
            }
            
            for (int i = 0; i < cells.size(); i++) {
                String text = cells.get(i).text();
                int length = text.length();
                
                if (length > columnlens[i]) {
                    columnlens[i] = length;
                }
            }

        }

        borders = "\n|";
        for (int len : columnlens) {
            borders += "-".repeat(len + 2) + "|";
        }
        borders += "\n| ";

        for (org.jsoup.nodes.Element row : rows) {
            Elements columns = row.select("td");
            List<String> columnitems = new ArrayList<>();
            String columnstr = "";
            int index = 0;
            
            for (org.jsoup.nodes.Element column : columns) {
                columnitems.add(column.wholeText());
                // columnstr += column.wholeText() + " | ";
                StringBuilder sb = new StringBuilder(column.wholeText());
                while (sb.length() < columnlens[index]) {
                    sb.append(" ");
                }
                sb.append(" | "); 
                columnstr += sb.toString();
                index++;
            }

            rowitems.add(columnitems);
            // rowstr += columnstr + "\n|-------------------------------------------------------------------------------|\n| ";
            rowstr += columnstr + borders;
        }

        System.out.println(rowstr);
        for (int contents : columnlens) {
            System.out.println(contents);
        }


        return "nothing to see here";

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

