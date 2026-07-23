package com.example;

import static dev.tamboui.toolkit.Toolkit.*;

import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;

import java.io.File;

import com.example.Indexer.SearchResult;

import dev.tamboui.toolkit.elements.MarkupTextAreaElement;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.common.ScrollBarPolicy; 

import dev.tamboui.widgets.input.TextInputState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.nodes.Document;


public class Main extends ToolkitApp {
    private static final TextInputState searchState = new TextInputState(); 
    private static String title = "functions.html";
    private static Document vieweddoc = Viewer.stylizeText(Viewer.getText(new File("app/src/main/java/com/example/functions.html")));
    private static String content = vieweddoc.body().wholeText();
    private static String match;

    private Viewer viewer = new Viewer();
    private MarkupTextAreaElement browser = viewer.registerActions(markupTextArea(content), vieweddoc);

    @Override
    protected TuiConfig configure() {
        return TuiConfig.builder()
                .mouseCapture(true)
                .build();
    }

    @Override
    protected Element render() {
        return panel(
            title,
            panel(
                browser
                    .scrollbar(ScrollBarPolicy.AS_NEEDED)
                    .borderType(BorderType.NONE)
                    .focusable()
                    .wrapWord()
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
                    browser.markup(Viewer.stylizeText(doc).body().wholeText());
                    browser.state().scrollToLine(line);

                });

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("apple.awt.UIElement", "true");

        Logger logger = Logger.getLogger("org.apache.lucene");
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        Main main = new Main();

        main.run();
    }
}
