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

import de.vandermeer.asciitable.AsciiTable;

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
        AsciiTable at = new AsciiTable();

        Elements ths = table.select("ths");
        if (!ths.isEmpty()) {
            String[] headers = ths.stream().map(org.jsoup.nodes.Element::text).toArray(String[]::new);
            at.addRule();
            at.addRow((Object[]) headers);
        }

        Elements rows = table.select("tr");
        for (org.jsoup.nodes.Element row : rows) {
            Elements tds = row.select("td");
            if (!tds.isEmpty()) {
                String[] cellData = tds.stream().map(org.jsoup.nodes.Element::text).toArray(String[]::new);
                at.addRule();
                at.addRow((Object[]) cellData);
            }
        }

        at.addRule();
        System.out.println(at.render());


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

