package com.example.model;

import java.util.List;

public class Definition {
    private String id;
    private List<String> signature;
    private String definition;

    public Definition() {}

    public Definition(String newid, List<String> signatures, String def) {
        this.id = newid;
        this.signature = signatures;
        this.definition = def;
    }

    public void setId(String newid) {
        this.id = newid;
    }

    public void setSignature(List<String> newsignature){
        this.signature = newsignature;
    }

    public void setDefinition(String newdef){
        this.definition = newdef;
    }

    public String getId() {
        return id;
    }

    public List<String> getSignature(){
        return this.signature;
    }

    public String getDefinition() {
        return this.definition;
    }
}
