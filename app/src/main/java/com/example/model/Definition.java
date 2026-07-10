package com.example.model;

import java.util.List;

public class Definition {
    private String location;
    private String type;
    private String id;
    private List<String> signature;
    private String definition;

    public Definition() {}

    public Definition(String newlocation, String newtype, String newid, List<String> signatures, String def) {
        this.location = newlocation;
        this.type = newtype;
        this.id = newid;
        this.signature = signatures;
        this.definition = def;
    }

    public void setLocation(String newlocation) {
        this.location = newlocation;
    }

    public void setType(String newType){
        this.type = newType;
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

    public String getLocation(){
        return this.location;
    }

    public String getType() {
        return type;
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
