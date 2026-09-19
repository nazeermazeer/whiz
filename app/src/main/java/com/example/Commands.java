package com.example;

import java.util.List;

public class Commands {
    private static final List<String> commands = List.of(
        "/help"
    );

    public static final List<String> searchCommands(String search) {
        return commands.stream()
            .filter(command -> command.startsWith("/"))
            .toList();
    }
}
