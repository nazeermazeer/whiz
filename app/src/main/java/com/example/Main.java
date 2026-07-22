package com.example;

import static dev.tamboui.toolkit.Toolkit.*;

import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;

import java.io.File;

import com.example.Indexer.SearchResult;

import dev.tamboui.toolkit.elements.MarkupTextAreaElement;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.common.ScrollBarPolicy; 

import dev.tamboui.widgets.input.TextInputState;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.nodes.Document;


public class Main extends ToolkitApp {
    private static final TextInputState searchState = new TextInputState();  
    private static String title = "functions.html";
    private static String content = Viewer.stylizeText(Viewer.getText(new File("app/src/main/java/com/example/functions.html"))).body().wholeText();
    private static String match;

    private MarkupTextAreaElement document = markupTextArea(content);

    @Override
    protected Element render() {
        return panel(
            title,
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

    private final Element searchbar = 
            textInput(searchState)
                .placeholder(Viewer.getRubbishText() + "...")
                .onSubmit(() -> {
                    String input = searchState.text();
                    Document doc = null;
                    match = "";
                    content = "";
                    try {
                        List<SearchResult> results = Indexer.searchTerm(input);
                        for (SearchResult result : results) {
                            if (match == "") { 
                                match = result.term()[0];
                                doc = Viewer.getText(new File("app/src/main/java/com/example/" + String.join(" ", result.location())));  
                                title = String.join(" ", result.location());
                            }
                        }
                    } catch (Exception err) {
                        throw new RuntimeException(err);
                    }   

                    int line = Viewer.getLine(doc.body().wholeText(), String.join(" ", match));
                    document.markup(Viewer.stylizeText(doc).body().wholeText());
                    document.state().scrollToLine(line);    

                });

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("apple.awt.UIElement", "true");

        Logger logger = Logger.getLogger("org.apache.lucene");
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        new Main().run();
    }
}
