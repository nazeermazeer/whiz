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
    private final Map<String, Runnable> actions = new HashMap<>(); 
    private static String title = "functions.html";
    private static String content = Viewer.stylizeText(Viewer.getText(new File("app/src/main/java/com/example/functions.html"))).body().wholeText();
    private static String match;

    private MarkupTextAreaElement document = markupTextArea(content);

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
                document
                    // Action hit-testing currently uses logical rows, so do
                    // not wrap lines until visual-row hit-testing is added.
                    .clip()
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
    
    private void registerActions() {
        actions.put("#abs", () ->
            document.state().scrollToLine(Viewer.getLine(content, "abs(")));

        actions.put("#aiter", () ->
            document.state().scrollToLine(Viewer.getLine(content, "aiter(")));

        actions.put("#iter", () ->
            document.state().scrollToLine(Viewer.getLine(content, "iter(")));

        actions.forEach(document::action);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("apple.awt.UIElement", "true");

        Logger logger = Logger.getLogger("org.apache.lucene");
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        Main main = new Main();
        main.registerActions();

        main.run();
    }
}
