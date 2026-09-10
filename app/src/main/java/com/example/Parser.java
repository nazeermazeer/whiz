package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import com.example.model.Entry;



public final class Parser {
    private String parseType(Element entry) {
        if (entry.attr("class").equals("py function")) {
            return "function";
        } else if (entry.attr("class").equals("py class")) {
            return "class";
        } else if (entry.attr("class").equals("py method")) {
            return "method";
        }
        return "";
    }

    private String parseAnchor(Element entry) {
        return entry.attr("id");
    }

    private String parseDefinition(Element entry) {
        for (Element element : entry.children()) {
            if (element.tagName().equals("dd")) {
                return element.text();
            }
        }

        return "";
    }

    private String getParent(String anchor) {
        String parent;
        try {
            parent = anchor.substring(0, anchor.indexOf("."));
        } catch (StringIndexOutOfBoundsException err) {
            // no parent exists for this element
            parent = "";
        }

        return parent;

    }

    private String[] gatherKeywords(Boolean parentExists, String anchor) {
        String[] keywords;
        if (parentExists) {
            keywords = new String[3];
            keywords[0] = anchor;
            keywords[1] = anchor.substring(anchor.lastIndexOf(".") + 1);
            keywords[2] = anchor.substring(anchor.lastIndexOf(".") + 1) + "()";
        } else {
            keywords = new String[2];
            keywords[0] = anchor.substring(anchor.lastIndexOf(".") + 1);
            keywords[1] = anchor.substring(anchor.lastIndexOf(".") + 1) + "()";
        }
        return keywords;
    }

    private List<String> parseSignatures(Element entry) {
        List<String> terms = new ArrayList<>();
        for (Element element : entry.children()) {
            if (element.tagName().equals("dt")) {
                terms.add(element.text().replace("¶", ""));
            }
        }
        return terms;
    }


    public void parseFiles() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Entry> jsonvalues = new ArrayList<>();

        for (int file = 1; file <= 2; file++) {
            File html;
            if (file == 1) {
                html = new File(
                    "app/src/main/java/com/example/functions.html"
                );
            } else {
                html = new File(
                    "app/src/main/java/com/example/stdtypes.html"
                );
            }

            Document doc = Jsoup.parse(html, "UTF-8");
            Elements dls = doc.select("dl");
            for (Element dl : dls) {
                String type = parseType(dl);

                String anchor = parseAnchor(dl);

                List<String> terms = parseSignatures(dl);

                String def = parseDefinition(dl);

                String parent = getParent(anchor);

                String[] keywords = gatherKeywords(parent != "", anchor);

                if (!anchor.equals("") && !type.equals("")) {
                    jsonvalues.add(
                        new Entry(html.getName(), type, ("python:" + anchor), anchor, parent, keywords, terms, def)
                    );
                }
            }
        }

        try {
            File outputfile = new File(
                "app/src/main/java/com/example/entries.json"
            );
            mapper.writerWithDefaultPrettyPrinter().writeValue(
                outputfile, jsonvalues
            );
        } catch (IOException err) {
            throw new UncheckedIOException(err);
        }
    }  

    public static void main(String[] args) throws IOException {
        Parser myparser = new Parser();
        myparser.parseFiles();
    }
}

