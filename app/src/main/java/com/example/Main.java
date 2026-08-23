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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final class Page {
        public final File file;
        public final String title;
        public final Document rawdoc;
        public final Document styleddoc;

        public Page(File file) {
            this.file = file;
            this.title = Viewer.getTitle(file);
            this.rawdoc = Viewer.getText(file);
            this.styleddoc = Viewer.stylizeText(rawdoc);
        }

        public String getContent() {
            return styleddoc.body().wholeText();
        }
    }
    private record SuggestionState(ListElement<?> element, List<SearchResult> results, int numresults, int height) {}

    private static final TextInputState SEARCHSTATE = new TextInputState();
    private static Page page = new Page(new File("app/src/main/java/com/example/functions.html"));
    private static String query = "";
    private static String match;
    private static ListElement<?> sidebar = createSidebar();
    private static SuggestionState suggestions = createSuggestions("");
    private static Element suggestionsPanel = panel(suggestions.element()).rounded();
    private static Indexer indexer = new Indexer();

    private static MarkupTextAreaElement createBrowser(Document document) {
        String content = document.body().wholeText();
        return Viewer.registerActions(markupTextArea(content), document);
    }

    private static MarkupTextAreaElement browser = createBrowser(page.styleddoc);

    private static final Element searchbar =
            textInput(SEARCHSTATE)
                .id("searchbar")
                .placeholder(Viewer.getRubbishText() + "...")
                .onSubmit(() -> {
                    match = "";
                    try {
                        int selected = suggestions.element().selected();
                        if (selected < 0 || selected >= suggestions.results().size()) {
                            return;
                        }
                        SearchResult result = suggestions.results().get(selected);

                        match = result.term()[0];
                        File file = new File(
                            "app/src/main/java/com/example/"
                            + String.join(" ", result.location())
                        );

                        int line = Viewer.getLine(
                            page.rawdoc.body().wholeText(),
                            String.join(" ", match)
                        );

                        page = new Page(file);
                        browser = Viewer.registerActions(browser, page.styleddoc);
                        browser.markup(page.getContent());
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
            page.file.getName()
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
                page.rawdoc.body().wholeText(), signature
            );
            browser.state().scrollToLine(line);

            return EventResult.HANDLED;
        });

        return newsidebar;
    }

    private static ListElement<?> getSidebarElement(List<String> anchors) {
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

    private static SuggestionState createSuggestions(String query) {
        ListElement<?> newsuggestions = list();
        List<SearchResult> suggestionResults = new ArrayList<>();
        List<SearchResult> searchedresults = new ArrayList<>();
        final int resultCount;

        if (query.isBlank()) {
            resultCount = 0;
        } else {
            try {
                searchedresults = indexer.searchTerm(query);
                Collections.reverse(searchedresults);
            } catch (org.apache.lucene.queryparser.classic.ParseException err) {
            } catch (IOException err) {
                throw new RuntimeException(err);
            }

            if (searchedresults.isEmpty()) {
                newsuggestions.add(text("No results found"));
                newsuggestions.displayOnly();
                resultCount = 1;
            } else {
                for (SearchResult result : searchedresults) {
                    for (String term : result.term()) {
                        newsuggestions.add(text(term));
                        suggestionResults.add(result);
                    }
                }

                resultCount = suggestionResults.size();
            }
        }
        int panelHeight;
        if (resultCount == 0) {
            panelHeight = 0;
        } else if (resultCount == 1 && searchedresults.isEmpty()) {
            panelHeight = 3;
        } else {
            panelHeight = Math.min(resultCount + 2, 15);
        }


        newsuggestions
            .highlightColor(Color.CYAN)
            .scrollbar(ScrollBarPolicy.AS_NEEDED)
            .autoScroll()
            .selectLast(resultCount);

        newsuggestions.onMouseEvent(event -> {
            if (event.kind() == MouseEventKind.SCROLL_DOWN) {
                newsuggestions.selectPrevious();
                return EventResult.HANDLED;
            }
            if (event.kind() == MouseEventKind.SCROLL_UP) {
                newsuggestions.selectNext(resultCount);
                return EventResult.HANDLED;
            }
            return EventResult.UNHANDLED;
        });

        return new SuggestionState(newsuggestions, suggestionResults, resultCount, panelHeight);

    }


    private static Element focusedSuggestions(int height) {
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
                return Size.of(panelSize.widthOr(0), height);
            }

            @Override
            public Constraint constraint() {
                return null;
            }
        };
    }

    private void indexEntries() {
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
            suggestionsPanel = panel(suggestions.element()).rounded();
        }

        return panel(
            page.title,
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
                    focusedSuggestions(suggestions.height()),
                    panel(searchbar)
                        .rounded()
                ).fill()
            )
        ).borderType(BorderType.NONE).fill();
    }
}
