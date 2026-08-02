package com.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Definition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Sidebar {
    public static List<String> getItems(File target) {
        ObjectMapper mapper = new ObjectMapper();
        List<String> anchors = new ArrayList<>();
        try {
            List<Definition> entries = mapper.readValue(target, new TypeReference<List<Definition>>(){});
            for (Definition entry : entries) {
                anchors.add(entry.getAnchor());
            }
        } catch (IOException e) {
            throw new RuntimeException();
        
        }

        return anchors;
    }
}
