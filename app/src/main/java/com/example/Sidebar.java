package com.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Definition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Sidebar {
    public record Item(String anchor, String signature) {}

    public static List<Item> getItems(File target, String filename) {
        ObjectMapper mapper = new ObjectMapper();
        List<Item> items = new ArrayList<>();
        try {
            List<Definition> entries = mapper.readValue(target, new TypeReference<List<Definition>>(){});
            for (Definition entry : entries) {
                if (entry.getLocation().equals(filename))
                    items.add(new Item(entry.getAnchor(), entry.getSignature().getFirst()));
            }
        } catch (IOException e) {
            throw new RuntimeException();
        
        }

        return items;
    }
}
