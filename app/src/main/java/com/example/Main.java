package com.example;

import static dev.tamboui.toolkit.Toolkit.*;

import dev.tamboui.style.Color;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;

import java.io.File;

import com.example.Indexer.SearchResult;
import com.example.Sidebar.Item;

import dev.tamboui.toolkit.elements.ListElement;
import dev.tamboui.toolkit.elements.MarkupTextAreaElement;
import dev.tamboui.toolkit.event.EventResult;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.event.MouseEventKind;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.common.ScrollBarPolicy; 

import dev.tamboui.widgets.input.TextInputState;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.nodes.Document;


public class Main extends ToolkitApp {
    private static final TextInputState searchState = new TextInputState(); 
    private static File file = new File("app/src/main/java/com/example/functions.html");
    private static String title = Viewer.getTitle(file);
    private static Document unstylizeddoc = Viewer.getText(file);
    private static Document currentdoc = Viewer.stylizeText(unstylizeddoc);

    private static String content = currentdoc.body().wholeText();
    private static String match;

    public Indexer indexer = new Indexer();

    private static MarkupTextAreaElement browser = Viewer.registerActions(markupTextArea(content), currentdoc);
    private static ListElement<?> sidebar = createSidebar();

    private static ListElement<?> createSidebar() {
        List<Item> items = Sidebar.getItems(new File("app/src/main/java/com/example/entries.json"), file.getName());
        List<String> anchors = new ArrayList<>();
        for (Item item : items) 
            anchors.add(item.anchor());
        ListElement<?> sidebar = getSidebarElement(anchors);

        sidebar.onKeyEvent(event -> {
            if (!event.isConfirm()) {
                return EventResult.UNHANDLED;
            }

            String anchor = anchors.get(sidebar.selected());
            String signature = items.stream()
                .filter(r -> r.anchor().equals(anchor))
                .map(Item::signature)
                .findFirst()
                .orElse(null);
            int line = Viewer.getLine(unstylizeddoc.body().wholeText(), signature);
            browser.state().scrollToLine(line);

            return EventResult.HANDLED;
        });

        return sidebar;
    }
            

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
            row(
                panel(sidebar)
                    .focusable()
                    .rounded(), 
                spacer(1),
                column(
                    spacer(1),
                    browser
                        .scrollbar(ScrollBarPolicy.AS_NEEDED)
                        .borderType(BorderType.NONE)
                        .focusable()
                        .wrapWord(),
                    panel(searchbar)
                        .rounded()
                ).fill()
            )
        ).borderType(BorderType.NONE).fill();
    }

    private final Element searchbar = 
            textInput(searchState)
                .placeholder(Viewer.getRubbishText() + "...")
                .onSubmit(() -> {
                    String input = searchState.text();  
                    match = "";
                    content = "";
                    try {
                        List<SearchResult> results = indexer.searchTerm(input);
                        for (SearchResult result : results) {
                            if (match == "") { 
                                match = result.term()[0];
                                file = new File("app/src/main/java/com/example/" + String.join(" ", result.location()));
                                unstylizeddoc = Viewer.getText(file);  
                                title = Viewer.getTitle(file);
                            }
                        }
                        int line = Viewer.getLine(unstylizeddoc.body().wholeText(), String.join(" ", match));
                        currentdoc = Viewer.stylizeText(unstylizeddoc);
                        browser = Viewer.registerActions(browser, currentdoc);
                        browser.markup(currentdoc.body().wholeText());
                        browser.state().scrollToLine(line);
                        sidebar = createSidebar();
                    } catch (Exception err) {
                        throw new RuntimeException(err);
                    }
                });

    public void indexEntries() throws Exception {
        indexer.indexEntries();
    }

    public static ListElement<?> getSidebarElement(List<String> anchors) {
        ListElement<?> list = list()
            .highlightColor(Color.CYAN)
            .autoScroll();
        for (String anchor : anchors) {
            list.add(text(anchor));
        }

        list.onMouseEvent(event -> {
            if (event.kind() == MouseEventKind.SCROLL_UP) {
                list.selectPrevious();
                return EventResult.HANDLED;
            }
            if (event.kind() == MouseEventKind.SCROLL_DOWN) {
                list.selectNext(anchors.size());
                return EventResult.HANDLED;
            }
            return EventResult.UNHANDLED;
        });

        return list;
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
