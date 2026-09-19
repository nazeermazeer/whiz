package com.example;

import static dev.tamboui.toolkit.Toolkit.markupTextArea;
import dev.tamboui.toolkit.elements.MarkupTextAreaElement;

import java.util.List;

public class Commands {
    private static final List<String> commands = List.of(
        "/help"
    );

    private static final MarkupTextAreaElement helpPanel = markupTextArea(
        "no help for you"
    );

    public static final List<String> searchCommands(String search) {
        return commands.stream()
            .filter(command -> command.startsWith("/"))
            .toList();
    }

    public static final boolean CommandExists(String command) {
        return commands.contains(command);
    }
    public static final MarkupTextAreaElement getPanelFromCommand(String command) {
        if (command.equals("/help")) {
            return helpPanel;
        }
        return null;
    }
}
