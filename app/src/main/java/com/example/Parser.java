package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import com.example.model.Definition;



public class Parser {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        List<Definition> jsonvalues = new ArrayList<>();

        for (int file = 1; file <= 2; file++) {
            try {
                File html;
                if (file == 1) {
                    html = new File("app/src/main/java/com/example/functions.html");
                } else {
                    html = new File("app/src/main/java/com/example/stdtypes.html");
                }
                Document doc = Jsoup.parse(html, "UTF-8");


                Elements dls = doc.select("dl");
                Element dl;


                for (int i = 0; i < dls.size(); i++) {
                    dl = dls.get(i);
                    List<String> terms = new ArrayList<>();
                    String def = "";
                    String type = "";
                    String anchor = "";
                    String parent = "";
                    List<String> keywords = new ArrayList<>();

                    if (dl.attr("class").equals("py function")) 
                        type = "function";
                    else if (dl.attr("class").equals("py class")) {
                        type = "class";
                    } else if (dl.attr("class").equals("py method")) {
                        type = "method";
                    }

                    anchor = dl.attr("id");


                    if (dl != null) {
                        for (Element element : dl.children()) {
                            if (element.tagName().equals("dt")) {
                                if (!element.attr("id").isBlank()) {
                                    anchor = element.attr("id");
                                }
                                terms.add(element.text());
                            } else if (element.tagName().equals("dd")) {
                                def = element.text();
                            }
                        }
                    }

                    try {
                        parent = anchor.substring(0, anchor.indexOf("."));
                    } catch (StringIndexOutOfBoundsException err) {
                        parent = null;
                    }

                    if (parent != null)
                        keywords.add(anchor);
                    keywords.add(anchor.substring(anchor.lastIndexOf(".") + 1));
                    keywords.add(anchor.substring(anchor.lastIndexOf(".") + 1) + "()");

                    if (!anchor.equals("") && !type.equals(""))
                        jsonvalues.add(new Definition(html.getName(), type, ("python:" + anchor), anchor, parent, keywords, terms, def));
                }
            } catch (IOException err) {
                System.out.println("Cannot read file");
            }
        }
        try{

            File outputfile = new File("app/src/main/java/com/example/entries.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(outputfile, jsonvalues);

        
        } catch (IOException err) {
            System.out.println("cannot read file");
        } 

            
        } 

    }

