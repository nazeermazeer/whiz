package com.example.model;

import java.util.List;

public class Value {
    private List<Definition> definitions;

    public void setDefinitions(List<Definition> newdefs) {
        this.definitions = newdefs;
    }

    public List<Definition> getDefinitions() {
        return this.definitions;
    }
}
