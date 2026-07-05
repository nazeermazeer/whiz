package com.example.model;

import java.util.List;

public class Definition {
    private List<String> signature;
    private String definition;

    public Definition() {}

    public Definition(List<String> signatures, String def) {
        this.signature = signatures;
        this.definition = def;
    }

    public void setSignature(List<String> newsignature){
        this.signature = newsignature;
    }

    public void setDefinition(String newdef){
        this.definition = newdef;
    }

    public List<String> getSignature(){
        return this.signature;
    }

    public String getDefinition() {
        return this.definition;
    }
}
