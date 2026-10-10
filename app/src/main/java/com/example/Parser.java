package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import com.example.model.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class Parser {
    private static final Logger logger = LogManager.getLogger(Parser.class);

    private record ParserItem(String name, InputStream stream) {};
    private static ParserItem[] items = {
        new ParserItem("functions", Parser.class.getResourceAsStream("/functions.html")),
        new ParserItem("stdtypes", Parser.class.getResourceAsStream("/stdtypes.html")),
        new ParserItem("constants", Parser.class.getResourceAsStream("/constants.html"))
    };

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

        for (ParserItem item : items) {
                Document doc = Jsoup.parse(item.stream, "UTF-8", "https://docs.python.org/3/");
                Elements dls = doc.select("dl");
                logger.debug("parsing {} documentation blocks from {}", dls.size(), item.name);
                for (Element dl : dls) {
                    String type = parseType(dl);
                    String anchor = parseAnchor(dl);
                    List<String> terms = parseSignatures(dl);
                    String def = parseDefinition(dl);
                    String parent = getParent(anchor);
                    String[] keywords = gatherKeywords(parent != "", anchor);

                    if (!anchor.equals("") && !type.equals("")) {
                        entries.add(
                            new Entry(item.name, type, ("python:" + anchor), anchor, parent, keywords, terms, def)
                        );
                    }
                }
        }

        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        File dir;

        if (os.contains("mac")) {
            dir = new File(home, "Library/Application Support/whiz");
        } else if (os.contains("win")) {
            dir = new File(System.getenv("APPDATA"), "whiz");
        } else {
            dir = new File(home, "whiz");
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File outputfile = new File(dir, "entries.json");
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
