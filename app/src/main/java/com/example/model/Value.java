package com.example.model;

import java.util.List;

public class Value {
    private List<Definition> definitions;

    public Value() {}

    public Value(List<Definition> defs) {
        this.definitions = defs;
    }

    public void setDefinitions(List<Definition> newdefs) {
        this.definitions = newdefs;
    }

    public List<Definition> getDefinitions() {
        return this.definitions;
    }
}
