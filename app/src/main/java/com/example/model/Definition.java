package com.example.model;

import java.util.List;

public class Definition {
    private List<String> term;
    private String definition;

    public Definition() {}

    public Definition(List<String> terms, String def) {
        this.term = terms;
        this.definition = def;
    }

    public void setTerm(List<String> newterm){
        this.term = newterm;
    }

    public void setDefinition(String newdef){
        this.definition = newdef;
    }

    public List<String> getTerm(){
        return this.term;
    }

    public String getDefinition() {
        return this.definition;
    }
}
