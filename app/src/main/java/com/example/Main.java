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

import dev.tamboui.toolkit.elements.MarkupTextAreaElement;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.common.ScrollBarPolicy;

import dev.tamboui.widgets.input.TextInputState;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.Random;
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
                .placeholder(this.getRubbishText());


    public static String getText() {
        File html = new File("app/src/main/java/com/example/functions.html");
        String text;

        try {
            Document doc = Jsoup.parse(html, "UTF-8");
            text = doc.body().text();
        } catch (IOException err) {
            text = "could not read file";
        } 

        return text;
    }

    public String getRubbishText() {
        Random myrandom = new Random();
        List<String> entries = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("app/src/main/java/com/example/rubbish.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                entries.add(line);
            }
        } catch (IOException err) {
            entries.add("rubbish.txt went wrong");
        }

        int randomIndex = myrandom.nextInt(entries.size());
        String line = entries.get(randomIndex);
    

        return line;
    }


    public static void main(String[] args) throws Exception {
        new Main().run();
    }
}

