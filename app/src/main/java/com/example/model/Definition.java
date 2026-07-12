package com.example.model;

import java.util.List;

public class Definition {
    private String location;
    private String type;
    private String id;
    private String anchor;
    private String parent;
    private List<String> keywords;
    private List<String> signature;
    private String definition;

    public Definition() {}

    public Definition(String location, String type, String id, String anchor, String parent, List<String> keywords, List<String> signatures, String def) {
        this.location = location;
        this.type = type;
        this.id = id;
        this.anchor = anchor;
        this.parent = parent;
        this.keywords = keywords;
        this.signature = signatures;
        this.definition = def;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void setSignature(List<String> signature){
        this.signature = signature;
    }

    public void setDefinition(String def){
        this.definition = def;
    }

    public String getLocation(){
        return this.location;
    }

    public String getType() {
        return this.type;
    }

    public String getId() {
        return this.id;
    }

    public String getAnchor() {
        return this.anchor;
    }

    public String getParent() {
        return this.parent;
    }

    public List<String> getKeywords() {
        return this.keywords;
    }

    public List<String> getSignature(){
        return this.signature;
    }

    public String getDefinition() {
        return this.definition;
    }
}
