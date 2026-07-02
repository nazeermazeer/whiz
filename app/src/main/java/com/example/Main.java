package com.example;

import static dev.tamboui.toolkit.Toolkit.*;
import dev.tamboui.style.Style;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.widgets.paragraph.Paragraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import dev.tamboui.toolkit.elements.MarkupTextAreaElement;
import dev.tamboui.widgets.common.ScrollBarPolicy;

public class Main extends ToolkitApp {
    private static final Path PATH = Path.of("app/src/main/java/com/example/functions.txt");
    private static final String TEXT = getText();


    @Override
    protected Element render() {
        return document;
    }

    private final MarkupTextAreaElement document = markupTextArea(TEXT)
            .wrapWord()
            .title(PATH.getFileName().toString())
            .rounded()
            .scrollbar(ScrollBarPolicy.AS_NEEDED)
            .id("document")
            .focusable();


    public static String getText() {
        String text;
        try {
            text = Files.readString(PATH);
        } catch (IOException err) {
            text = "could not read file";
        } 

        return text;
    }


    public static void main(String[] args) throws Exception {
        new Main().run();
    }
}
