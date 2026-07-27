package com.example;

import static dev.tamboui.toolkit.Toolkit.*;

import dev.tamboui.style.Color;
import dev.tamboui.style.Style;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;

import java.io.File;

import com.example.Indexer.SearchResult;

import dev.tamboui.toolkit.elements.ListElement;
import dev.tamboui.toolkit.elements.MarkupTextAreaElement;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.widgets.block.Block;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.block.Borders;
import dev.tamboui.widgets.common.ScrollBarPolicy; 

import dev.tamboui.widgets.input.TextInputState;
import dev.tamboui.widgets.list.ListItem;
import dev.tamboui.widgets.list.ListState;
import dev.tamboui.widgets.list.ListWidget;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.nodes.Document;


public class Main extends ToolkitApp {
    private static final TextInputState searchState = new TextInputState(); 
    private static File file = new File("app/src/main/java/com/example/functions.html");
    private static String title = Viewer.getTitle(file);
    private static Document currentdoc = Viewer.stylizeText(Viewer.getText(file));

    private static String content = currentdoc.body().wholeText();
    private static String match;

    private static MarkupTextAreaElement browser = Viewer.registerActions(markupTextArea(content), currentdoc);
    private static ListElement list = list()
        .add(row(text("*").yellow(), text(" Featured Item")))
        .add(text("Plain Item"))
        .add(panel("Nested Panel", text("Content")).rounded())
        .highlightColor(Color.CYAN)
        .autoScroll();

    @Override
    protected TuiConfig configure() {
        return TuiConfig.builder()
                .mouseCapture(true)
                .build();
    }
    public Indexer indexer = new Indexer();

    @Override
    protected Element render() {
        return panel(
            title,
            row(list, panel(panel(
                browser
                    .scrollbar(ScrollBarPolicy.AS_NEEDED)
                    .borderType(BorderType.NONE)
                    .focusable()
                    .wrapWord()
            ).borderType(BorderType.NONE),
            panel(searchbar).rounded()).borderType(BorderType.NONE))
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
                        List<SearchResult> results = indexer.searchTerm(input);
                        for (SearchResult result : results) {
                            if (match == "") { 
                                match = result.term()[0];
                                file = new File("app/src/main/java/com/example/" + String.join(" ", result.location()));
                                doc = Viewer.getText(file);  
                                title = Viewer.getTitle(file);
                            }
                        }
                        int line = Viewer.getLine(doc.body().wholeText(), String.join(" ", match));
                        Document newdoc = Viewer.stylizeText(doc);
                        browser = Viewer.registerActions(browser, newdoc);
                        browser.markup(newdoc.body().wholeText());
                        browser.state().scrollToLine(line);
                    } catch (Exception err) {
                        throw new RuntimeException(err);
                    }
                });

    public void indexEntries() throws Exception {
        indexer.indexEntries();
    }

    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger("org.apache.lucene");
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        Main main = new Main();
        main.indexEntries();

        main.run();
    }
}
