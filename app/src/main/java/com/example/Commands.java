package com.example;

import static dev.tamboui.toolkit.Toolkit.markupTextArea;
import dev.tamboui.toolkit.elements.MarkupTextAreaElement;

import java.util.List;

public class Commands {
    public record SlashCommand(String name, String description) {}
    private static final List<SlashCommand> commands = List.of(
        new SlashCommand("/help", "get help"),
        new SlashCommand("/stats", "get stats")
    );

    private static final MarkupTextAreaElement helpPanel = markupTextArea(
        "no help for you"
    );

    public static final List<SlashCommand> searchCommands(String search) {
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
            ))
            .toList();
    }

    public static final MarkupTextAreaElement getPanelFromCommand(String command) {
        if (command.equals("/help")) {
            return helpPanel;
        }
        return null;
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
}
