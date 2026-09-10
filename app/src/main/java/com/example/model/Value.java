package com.example.model;

import java.util.List;

public final class Value {
    private List<Entry> definitions;

    public Value() { }

    public Value(List<Entry> defs) {
        this.definitions = defs;
    }

    public void setDefinitions(List<Entry> newdefs) {
        this.definitions = newdefs;
    }

    public List<Entry> getDefinitions() {
        return this.definitions;
    }
}
