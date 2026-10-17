package com.example;


import static dev.tamboui.toolkit.Toolkit.column;
import static dev.tamboui.toolkit.Toolkit.list;
import static dev.tamboui.toolkit.Toolkit.panel;
import static dev.tamboui.toolkit.Toolkit.row;
import static dev.tamboui.toolkit.Toolkit.spacer;
import static dev.tamboui.toolkit.Toolkit.text;
import static dev.tamboui.toolkit.Toolkit.textInput;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import com.example.Commands.Commands;
import com.example.Commands.Statistics;
import com.example.Commands.Commands.SlashCommand;
import com.example.Parser.Entry;
import dev.tamboui.style.Color;
import dev.tamboui.layout.Constraint;
import dev.tamboui.layout.Rect;
import dev.tamboui.terminal.Frame;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.element.RenderContext;
import dev.tamboui.toolkit.element.Size;
import dev.tamboui.toolkit.elements.ListElement;
import dev.tamboui.toolkit.elements.ActionableMarkupTextAreaElement;
import dev.tamboui.toolkit.event.EventResult;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.tui.error.ErrorAction;
import dev.tamboui.tui.event.MouseEventKind;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.common.ScrollBarPolicy;
import dev.tamboui.widgets.input.TextInputState;

public final class Main extends ToolkitApp {
    private Indexer indexer;
    private Viewer viewer;
    private Parser parser;
    private Commands commands;

    private ListElement<?> sidebar;
    private SuggestionState suggestions;
    private ActionableMarkupTextAreaElement browser;
    private Element searchbar;

    private Page page;
    private String query = "";
    private TextInputState searchbarstate = new TextInputState();

    private static Path appdir;
    private static final Logger logger = LogManager.getLogger(Main.class);

    public Main(Indexer newindexer, Viewer newviewer, Parser newparser, Commands newcommands) {
        indexer = newindexer;
        viewer = newviewer;
        parser = newparser;
        commands = newcommands;

        page = new Page(new File(Main.getAppDirectory(), "functions.html"));

        try {
            parser.parseFiles();
            indexer.indexEntries();
            sidebar = createSidebarPanel();
        } catch (IOException err) {
            throw new RuntimeException(err);
        }

        suggestions = createSuggestionsPanel("");
        browser = createBrowserPanel(page.styleddoc);
        searchbar = createSearchbarPanel(searchbarstate);
    }

    private final class Page {
        public final File file;
        public final String title;
        public final Document rawdoc;
        public final Document styleddoc;

        public Page(File file) {
            this.file = file;

            viewer.loadDocument(file);
            this.title = viewer.getDocumentTitle();
            this.rawdoc = viewer.getRawDocument();
            this.styleddoc = viewer.getStylizedDocument();
        }

        public String getContent() {
            return styleddoc.body().wholeText();
        }
    }

    private record SuggestionState(ListElement<?> element, List<Entry> results, int numresults, int height) {}

    private ActionableMarkupTextAreaElement createBrowserPanel(Document document) {
        String content = document.body().wholeText();
        return viewer.registerElementActions(new ActionableMarkupTextAreaElement(content));
    }            

    private Element createSearchbarPanel(TextInputState state) {
        return textInput(state)
            .id("searchbar")
            .placeholder(Viewer.getRubbishText() + "...")
            .onSubmit(() -> {
                String match = "";
                try {
                    int selected = suggestions.element().selected();
                    if (selected < 0 || selected >= suggestions.results().size()) {
                        return;
                    }
                    Entry result = suggestions.results().get(selected);
                    match = result.signature().get(0);
                    if (match.startsWith("/")) {
                        browser = commands.getPanelFromCommand(match);

                        logger.info("redirected user to '{}' panel", match);
                    } else {
                        File file = new File(Main.getAppDirectory(), String.join(" ", result.location()));

                        page = new Page(file);

                        browser = viewer.registerElementActions(browser);
                        browser.markup(page.getContent());
                        browser.state().scrollToLine(result.linenum());
                        sidebar = createSidebarPanel();

                        commands.recordTotalSearch();
                        commands.recordDocumentationSearch("python");

                        logger.info("redirected user to search result for \"{}\"", match);
                    }
                } catch (Exception err) {
                    throw new RuntimeException(err);
                }
            }
        );
    }

    private ListElement<?> createSidebarPanel() throws IOException {
        List<Entry> entries = indexer.readJSON();

        ListElement<?> list = list()
            .highlightColor(Color.CYAN)
            .autoScroll();

        for (Entry entry : entries) {
            if (entry.location().equals(page.file.getName())) {
                list.add(row(
                    text(entry.type().substring(0, 1).toUpperCase(), " ", entry.name())
                ));
            }
        }

        list.onMouseEvent(event -> {
            if (event.kind() == MouseEventKind.SCROLL_UP) {
                list.selectPrevious();
                return EventResult.HANDLED;
            }
            if (event.kind() == MouseEventKind.SCROLL_DOWN) {
                list.selectNext(entries.size());
                return EventResult.HANDLED;
            }
            return EventResult.UNHANDLED;
        });

        list.onKeyEvent(event -> {
            if (!event.isConfirm()) {
                return EventResult.UNHANDLED;
            }

            int line = entries.get(list.selected()).linenum();

            browser.state().scrollToLine(line);
            logger.info("redirected user to sidebar item for '{}'", line);

            return EventResult.HANDLED;
        });

        return list;
    }

    private SuggestionState createSuggestionsPanel(String query) {
        ListElement<?> newsuggestions = list();
        List<Entry> suggestionResults = new ArrayList<>();
        List<Entry> searchedresults = new ArrayList<>();
        final int resultCount;
        int panelHeight;

        if (query.isBlank()) {
            resultCount = 0;
        } else if (query.startsWith("/"))  {
            List<SlashCommand> slashcommands = commands.searchCommands(query);
            resultCount = slashcommands.size();
            for (SlashCommand command : slashcommands) {
                newsuggestions.add(row(text(command.name()).bold(), text(" " + command.description()).dim()));
                suggestionResults.add(new Entry(command.name(), command.description()));
            }
        } else {
            try {
                searchedresults = indexer.searchTerm(query);
                Collections.reverse(searchedresults);
            } catch (org.apache.lucene.queryparser.classic.ParseException err) {
                logger.error("could not parse search query '{}'", query, err);
            } catch (IOException err) {
                logger.error("could not search for query '{}'", query, err);
                throw new RuntimeException(err);
            }

            if (searchedresults.isEmpty()) {
                newsuggestions.add(text("No results found"));
                newsuggestions.displayOnly();
                resultCount = 1;
            } else {
                for (Entry result : searchedresults) {
                    newsuggestions.add(row(
                        text(result.type().substring(0, 1).toUpperCase(), " ", result.name()), 
                        text(" ", result.parent()).dim()
                    ));

                    suggestionResults.add(result);
                }

                resultCount = suggestionResults.size();
            }
        }

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


    private Element renderSuggestionsPanel(int height, Element panel) {
        return new Element() {
            private boolean isSearchbarFocused(RenderContext context) {
                return context != null
                    && context.isFocused(searchbar.id());
            }

            @Override
            public void render(Frame frame, Rect area, RenderContext context) {
                if (isSearchbarFocused(context)) {
                    context.renderChild(panel, frame, area);
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
                Size panelSize = panel.preferredSize(
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

    public static String getAppDirectory() {
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            appdir = Paths.get(home, "Library", "Application Support", "whiz");
        } else if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            appdir = Paths.get(appData != null ? appData : home, "whiz");
        } else {
            appdir = Paths.get(home, ".whiz");
        }

        try {
            Files.createDirectories(appdir);
        } catch (IOException err) {
            throw new UncheckedIOException(err);
        }

        return appdir.toAbsolutePath().toString();
    } 

    public static void main(String[] args) throws Exception {
        java.util.logging.Logger lucenelogger = java.util.logging.Logger.getLogger("org.apache.lucene");
        lucenelogger.setLevel(Level.OFF);
        lucenelogger.setUseParentHandlers(false);

        logger.info("application started!");
    
        Indexer myindexer = new Indexer();

        Viewer myviewer = new Viewer();

        Parser myparser = new Parser(myviewer);

        Statistics mystatistics = new Statistics();
        Commands mycommands = new Commands(mystatistics);

        Main main = new Main(myindexer, myviewer, myparser, mycommands);
        main.run();
    }

    @Override
    protected Element render() {
        String currentQuery = searchbarstate.text();
        if (!currentQuery.equals(query)) {
            query = currentQuery;
            suggestions = createSuggestionsPanel(query);
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
                    renderSuggestionsPanel(suggestions.height(), panel(suggestions.element()).rounded()),
                    panel(searchbar)
                        .rounded()
                ).fill()
            )
        ).borderType(BorderType.NONE).fill();
    }

    @Override
    protected TuiConfig configure() {
        return TuiConfig.builder()
                .mouseCapture(true)
                .errorHandler((error, context) -> {
                    logger.fatal("crashed! throwing internal exception error widget...", error.cause());
                    return ErrorAction.DISPLAY_AND_QUIT;
                })
                .build();
    }

    @Override
    protected void onStop() {
        logger.info("application stopped");
        try {
            commands.saveStatistics();
        } catch (IOException err) {
            logger.error("failed to save statistics", err);
        }
    }
}
