package com.example;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Definition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Sidebar {
    private static final Logger logger = LogManager.getLogger(Sidebar.class);
    private Sidebar() {
        throw new UnsupportedOperationException(
            "This is a utility class and cannot be instantiated"
        );
    }
    public record Item(String anchor, String signature) { }

    public static List<Item> getItems(File target, String filename) {
        ObjectMapper mapper = new ObjectMapper();
        List<Item> items = new ArrayList<>();
        try {
            List<Definition> entries = mapper.readValue(
                target, new TypeReference<List<Definition>>() { }
            );
            for (Definition entry : entries) {
                if (entry.getLocation().equals(filename)) {
                    items.add(
                        new Item(entry.getAnchor(),
                        entry.getSignature().getFirst())
                    );
                }
            }
            logger.debug("loaded {} sidebar items for {}", items.size(), filename);
        } catch (IOException err) {
            logger.error("failed to load sidebar entries from {}", target, err);
            throw new UncheckedIOException(err);
        }

        return items;
    }
}
