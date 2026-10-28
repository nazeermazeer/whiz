package com.example.Commands;


import dev.tamboui.toolkit.elements.ActionableMarkupTextAreaElement;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.Main;

public class Commands {
    public record SlashCommand(String name, String description) {}
    private Statistics stats;

    public Commands(Statistics newstats) {
        this.stats = newstats;
        try {
            stats.loadStatistics();
        } catch (IOException err) {
            throw new RuntimeException(err);
        }
    }

    private static final List<SlashCommand> commands = List.of(
        new SlashCommand("/help", "get help"),
        new SlashCommand("/stats", "get stats"),
        new SlashCommand("/download", "install python documentation")
    );

    public final List<SlashCommand> searchCommands(String search) {
        String query = search.substring(1)
                        .trim()
                        .toLowerCase();

        if (query.isEmpty()) {
            return commands;
        }

        return commands.stream()
            .filter(command -> scoreCommandSearch(command, query) > 0)
            .sorted((a, b) -> Integer.compare(
                scoreCommandSearch(b, query),
                scoreCommandSearch(a, query)
            )).toList();
    }

    public final ActionableMarkupTextAreaElement getPanelFromCommand(String command) {
        if (command.equals("/help")) {
            return new ActionableMarkupTextAreaElement("no help for you");
        }

        if (command.equals("/stats")) {
            return new ActionableMarkupTextAreaElement(getStatisticsPanelContents(stats.getCreationDate(), stats.getTotalSearches(), stats.getDocumentSearches()));
        }

        if (command.equals("/download")) {
            Path path = Path.of(Main.getAppDirectory(), "python");
            Downloader.downloadWebsite("https://docs.python.org/3/", path);
            return new ActionableMarkupTextAreaElement("downloading... i think....");
        }
        return null;
    }

    private static String getStatisticsPanelContents(String creationDate, int totsearches, Map<String, Integer> docsearches) {
        String contents = "";
        
        LocalDateTime instant = LocalDateTime.parse(creationDate);
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("EEEE, MMMM dd, yyyy hh:mm a")
                .withZone(ZoneId.systemDefault())
                .withLocale(Locale.ENGLISH);

        contents += "since: " + formatter.format(instant) + "\n";
        contents += "total searches: " + totsearches + "\n";

        for (Map.Entry<String, Integer> doc : docsearches.entrySet()) {
            contents += "    " + doc.getKey() + " searches: " + doc.getValue();
        }

        return contents;
        
    }

    private static int scoreCommandSearch(SlashCommand command, String query) {
        String name = command.name().substring(1).toLowerCase();
        String description = command.description().toLowerCase();

        if (name.equals(query))
            return 100;

        if (name.startsWith(query))
            return 80;

        if (name.contains(query))
            return 60;

        if (description.contains(query))
            return 40;

        return 0;
    }

    public final void recordTotalSearch() {
        stats.increaseTotalSearches();
    }

    public final void recordDocumentationSearch(String doc) {
        stats.increaseDocumentSearches(doc);
    }

    public final void saveStatistics() throws IOException {
        stats.writeStatistics();
    }
}
