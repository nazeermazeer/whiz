package com.example;


import static dev.tamboui.toolkit.Toolkit.column;
import static dev.tamboui.toolkit.Toolkit.list;
import static dev.tamboui.toolkit.Toolkit.markupTextArea;
import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.row;
import static dev.tamboui.toolkit.Toolkit.spacer;
import static dev.tamboui.toolkit.Toolkit.text;
import static dev.tamboui.toolkit.Toolkit.textInput;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.RuntimeErrorException;

import org.jsoup.nodes.Document;
import com.example.Indexer.SearchResult;
import com.example.Sidebar.Item;
import dev.tamboui.style.Color;
import dev.tamboui.layout.Constraint;
import dev.tamboui.layout.Rect;
import dev.tamboui.terminal.Frame;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.element.RenderContext;
import dev.tamboui.toolkit.element.Size;
import dev.tamboui.toolkit.elements.ListElement;
import dev.tamboui.toolkit.elements.MarkupTextAreaElement;
import dev.tamboui.toolkit.event.EventResult;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.event.MouseEventKind;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.common.ScrollBarPolicy;
import dev.tamboui.widgets.input.TextInputState;

public final class Main extends ToolkitApp {
    private static final TextInputState SEARCHSTATE = new TextInputState();
    private static final int SUGGESTIONS_ITEM_COUNT = 15;
    private static final int SUGGESTIONS_HEIGHT = 12;
    private static File file = new File(
        "app/src/main/java/com/example/functions.html"
    );
    private static String title = Viewer.getTitle(file);
    private static String query = "";
    private static Document unstylizeddoc = Viewer.getText(file);
    private static Document currentdoc = Viewer.stylizeText(unstylizeddoc);
    private static String content = currentdoc.body().wholeText();
    private static String match;
    private static ListElement<?> sidebar = createSidebar();
    private static ListElement<?> suggestions = createSuggestions();
    private static Element suggestionsPanel = panel(suggestions).rounded();
    private static Indexer indexer = new Indexer();

    private static MarkupTextAreaElement browser = Viewer.registerActions(
        markupTextArea(content), currentdoc
    );

    private final Element searchbar =
            textInput(SEARCHSTATE)
                .id("searchbar")
                .placeholder(Viewer.getRubbishText() + "...")
                .onSubmit(() -> {
                    String input = SEARCHSTATE.text();
                    match = "";
                    content = "";
                    try {
                        List<SearchResult> results = indexer.searchTerm(input);
                        for (SearchResult result : results) {
                            if (match.equals("")) {
                                match = result.term()[0];
                                file = new File(
                                    "app/src/main/java/com/example/"
                                    + String.join(" ", result.location())
                                );
                                unstylizeddoc = Viewer.getText(file);
                                title = Viewer.getTitle(file);
                            }
                        }
                        int line = Viewer.getLine(
                            unstylizeddoc.body().wholeText(),
                            String.join(" ", match)
                        );

                        currentdoc = Viewer.stylizeText(unstylizeddoc);
                        browser = Viewer.registerActions(browser, currentdoc);
                        browser.markup(currentdoc.body().wholeText());
                        browser.state().scrollToLine(line);
                        sidebar = createSidebar();
                    } catch (Exception err) {
                        throw new RuntimeException(err);
                    }
                });


    @Override
    protected TuiConfig configure() {
        return TuiConfig.builder()
                .mouseCapture(true)
                .build();
    }

    private static ListElement<?> createSidebar() {
        List<Item> items = Sidebar.getItems(
            new File("app/src/main/java/com/example/entries.json"),
            file.getName()
        );
        List<String> anchors = new ArrayList<>();
        for (Item item : items) {
            anchors.add(item.anchor());
        }
        ListElement<?> newsidebar = getSidebarElement(anchors);

        newsidebar.onKeyEvent(event -> {
            if (!event.isConfirm()) {
                return EventResult.UNHANDLED;
            }

            String anchor = anchors.get(newsidebar.selected());
            String signature = items.stream()
                .filter(r -> r.anchor().equals(anchor))
                .map(item -> item.signature())
                .findFirst()
                .orElse(null);
            int line = Viewer.getLine(
                unstylizeddoc.body().wholeText(), signature
            );
            browser.state().scrollToLine(line);

            return EventResult.HANDLED;
        });

        return newsidebar;
    }

    public static ListElement<?> getSidebarElement(List<String> anchors) {
        ListElement<?> list = list()
            .highlightColor(Color.CYAN)
            .autoScroll();
        for (String anchor : anchors) {
            list.add(
                text(anchor)
            );
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

        public static ListElement<?> createSuggestions(String suggestion) {
            List<SearchResult> results;
        ListElement<?> newsuggestions = list();
        // for (String suggestion : suggestions) {
        try {
            results = indexer.searchTerm(suggestion);
        } catch (org.apache.lucene.queryparser.classic.ParseException err) {
            results = new ArrayList<>();
        } catch (IOException err) {
            throw new RuntimeException(err);
        }
        if (results.size() != 0) {
            newsuggestions.add(text(results.get(0).term()[0]));
        }
        // }
        newsuggestions
            .highlightColor(Color.CYAN)
            .scrollbar(ScrollBarPolicy.AS_NEEDED)
            .autoScroll();

        newsuggestions.onMouseEvent(event -> {
            if (event.kind() == MouseEventKind.SCROLL_UP) {
                newsuggestions.selectPrevious();
                return EventResult.HANDLED;
            }
            if (event.kind() == MouseEventKind.SCROLL_DOWN) {
                newsuggestions.selectNext(1);
                return EventResult.HANDLED;
            }
            return EventResult.UNHANDLED;
        });

        return newsuggestions;

    }

    public static ListElement<?> createSuggestions() {
        ListElement<?> newsuggestions = list();
        newsuggestions
            .add(text("Pariatur laborum incididunt id cillum proident ipsum."))
            .add(text("Consequat nulla esse nostrud."))
            .add(text("Labore nulla eiusmod ullamco culpa officia ex ullamco ea adipisicing esse Lorem."))
            .add(text("Eiusmod ex ut excepteur sunt duis exercitation aute irure quis do anim laboris aliqua."))
            .add(text("Eu velit laboris cillum."))
            .add(text("Eiusmod incididunt magna id ut enim sit nulla nisi esse."))
            .add(text("Laboris laborum mollit minim ut deserunt est eiusmod laboris consequat veniam ad dolor ea magna laborum."))
            .add(text("Nulla qui elit mollit."))
            .add(text("Velit ad qui irure nostrud non id aute ea excepteur ea."))
            .add(text("Excepteur deserunt aliquip do fugiat nisi labore dolor incididunt dolor."))
            .add(text("Pariatur reprehenderit Lorem pariatur deserunt voluptate aliqua cillum eu et duis sunt aliquip culpa."))
            .add(text("Velit aliquip nulla fugiat veniam eu ex sunt quis ad anim cillum qui."))
            .add(text("Enim ipsum tempor ea ut pariatur excepteur irure sunt."))
            .add(text("Voluptate laborum commodo non laborum non eu consequat est."))
            .add(text("Mollit ea eu aliquip aliquip excepteur ad occaecat laborum."))
            .highlightColor(Color.CYAN)
            .scrollbar(ScrollBarPolicy.AS_NEEDED)
            .autoScroll();

        newsuggestions.onMouseEvent(event -> {
            if (event.kind() == MouseEventKind.SCROLL_UP) {
                newsuggestions.selectPrevious();
                return EventResult.HANDLED;
            }
            if (event.kind() == MouseEventKind.SCROLL_DOWN) {
                newsuggestions.selectNext(SUGGESTIONS_ITEM_COUNT);
                return EventResult.HANDLED;
            }
            return EventResult.UNHANDLED;
        });

        return newsuggestions;

    }

    private Element focusedSuggestions() {
        return new Element() {
            private boolean isSearchbarFocused(RenderContext context) {
                return context != null
                    && context.isFocused(searchbar.id());
            }

            @Override
            public void render(Frame frame, Rect area, RenderContext context) {
                if (isSearchbarFocused(context)) {
                    context.renderChild(suggestionsPanel, frame, area);
                }
            }

            @Override
            public Size preferredSize(
                    int availableWidth,
                    int availableHeight,
                    RenderContext context
            ) {
                if (!isSearchbarFocused(context)) {
                    return Size.ZERO;
                }
                Size panelSize = suggestionsPanel.preferredSize(
                    availableWidth, availableHeight, context
                );
                return Size.of(panelSize.widthOr(0), SUGGESTIONS_HEIGHT);
            }

            @Override
            public Constraint constraint() {
                return null;
            }
        };
    }

    public void indexEntries() {
        try {
            indexer.indexEntries();
        } catch (IOException err) {
            throw new UncheckedIOException(err);
        }
    }

    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger("org.apache.lucene");
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        Main main = new Main();
        main.indexEntries();


        main.run();
    }

    @Override
    protected Element render() {
        String currentQuery = SEARCHSTATE.text();
        if (!currentQuery.equals(query)) {
            query = currentQuery;
            suggestions = createSuggestions(query);
            suggestionsPanel = panel(suggestions).rounded();
        }

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
                    focusedSuggestions(),
                    panel(searchbar)
                        .rounded()
                ).fill()
            )
        ).borderType(BorderType.NONE).fill();
    }
}
