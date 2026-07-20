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
import java.util.logging.Level;
import java.util.logging.Logger;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

public class Main extends ToolkitApp {
    private static final Path PATH = Path.of("app/src/main/java/com/example/functions.html");
    private String TEXT = Viewer.getText(new File("app/src/main/java/com/example/functions.html"));
    private final TextInputState searchState = new TextInputState();  

    private Indexer myindexer = new Indexer();


    @Override
    protected Element render() {
        return panel(
            PATH.getFileName().toString(),
            panel(
                document
                    .wrapWord()
                    .scrollbar(ScrollBarPolicy.AS_NEEDED)
                    .borderType(BorderType.NONE)
                    .focusable()
            ).borderType(BorderType.NONE),
            panel(searchbar)
        ).borderType(BorderType.NONE);
    }

    private MarkupTextAreaElement document = markupTextArea(TEXT);
    String match;

    private final Element searchbar = 
            textInput(searchState)
                .placeholder(Viewer.getRubbishText() + "...")
                .onSubmit(() -> {
                    String input = searchState.text();
                    match = "";
                    TEXT = "";
                    try {
                        List<SearchResult> results = myindexer.searchTerm(input);
                        for (SearchResult result : results) {
                            if (match == "") { 
                                match = result.term()[0];
                                TEXT = Viewer.getText(new File("app/src/main/java/com/example/" + String.join(" ", result.location())));    
                            }
                        }
                    } catch (Exception err) {
                        throw new RuntimeException(err);
                    }   

                    document.markup(TEXT);
                    int line = Viewer.getLine(TEXT, String.join(" ", match));
                    document.state().scrollToLine(line);

                });

    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger("org.apache.lucene");
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        new Main().run();
    }
}

