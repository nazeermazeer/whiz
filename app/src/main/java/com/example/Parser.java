package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class Parser {
    private static final Logger logger = LogManager.getLogger(Parser.class);

    public record Entry(
        String file, int linenum, String type, String id, String anchor, 
        String parent, String[] keywords, List<String> signature, String definition
    ) {}

    private String parseType(Element entry) {
        if (entry.attr("class").equals("py function")) {
            return "function";
        } else if (entry.attr("class").equals("py class")) {
            return "class";
        } else if (entry.attr("class").equals("py method") || entry.attr("class").equals("py data")) {
            return "method";
        }
        return "";
    }

    private String parseAnchor(Element entry) {
        for (Element element : entry.children()) {
            if (element.tagName().equals("dt")) {
                return element.attr("id");
            }
        }
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
        List<Entry> entries = new ArrayList<>();
        logger.info("starting documentation parse");

        File[] files = {
            new File(Main.getAppDirectory(), "functions.html"),
            new File(Main.getAppDirectory(), "stdtypes.html"),
            new File(Main.getAppDirectory(), "constants.html")
        };

        org.jsoup.parser.Parser htmlparser = org.jsoup.parser.Parser.htmlParser().setTrackPosition(true);

        for (File file : files) {
            try (InputStream stream = new FileInputStream(file)) {
                Document doc = Jsoup.parse(stream, "UTF-8", "https://docs.python.org/3/", htmlparser);
                Elements dls = doc.select("dl");
                logger.debug("parsing {} documentation blocks from {}", dls.size(), file);
                for (Element dl : dls) {
                    int line = dl.sourceRange().start().lineNumber();

                    String type = parseType(dl);
                    String anchor = parseAnchor(dl);
                    List<String> terms = parseSignatures(dl);
                    String def = parseDefinition(dl);
                    String parent = getParent(anchor);
                    String[] keywords = gatherKeywords(parent != "", anchor);

                    if (!anchor.equals("") && !type.equals("")) {
                        entries.add(
                            new Entry(file.getName(), line, type, ("python:" + anchor), anchor, parent, keywords, terms, def)
                        );
                    }
                }
            }
        }

        File outputfile = new File(Main.getAppDirectory(), "entries.json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(outputfile, entries);
            logger.info("wrote {} definitions to {}", entries.size(), outputfile);
        } catch (IOException err) {
            logger.error("failed to write parsed definitions", err);
            throw new UncheckedIOException(err);
        }
    }  
}
