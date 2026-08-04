package com.example.model;

import java.util.List;

public final class Definition {
    private String location;
    private String type;
    private String id;
    private String anchor;
    private String parent;
    private List<String> keywords;
    private List<String> signature;
    private String definition;

    public Definition() { }

    public Definition(
        String newlocation,
        String newtype,
        String newid,
        String newanchor,
        String newparent,
        List<String> newkeywords,
        List<String> newsignatures,
        String newdef
    ) {
        this.location = newlocation;
        this.type = newtype;
        this.id = newid;
        this.anchor = newanchor;
        this.parent = newparent;
        this.keywords = newkeywords;
        this.signature = newsignatures;
        this.definition = newdef;
    }

    public void setLocation(String newlocation) {
        this.location = newlocation;
    }

    public void setType(String newtype) {
        this.type = newtype;
    }

    public void setId(String newid) {
        this.id = newid;
    }

    public void setAnchor(String newanchor) {
        this.anchor = newanchor;
    }

    public void setParent(String newparent) {
        this.parent = newparent;
    }

    public void setKeywords(List<String> newkeywords) {
        this.keywords = newkeywords;
    }

    public void setSignature(List<String> newsignature) {
        this.signature = newsignature;
    }

    public void setDefinition(String newdef) {
        this.definition = newdef;
    }

    public String getLocation() {
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

    public List<String> getSignature() {
        return this.signature;
    }

    public String getDefinition() {
        return this.definition;
    }
}
