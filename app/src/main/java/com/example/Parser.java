package com.example;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class Parser {
    private static final Logger logger = LogManager.getLogger(Parser.class);

    public record Entry(
        String location, int linenum, String type, String id, String anchor, 
        String name, String parent, String[] keywords, List<String> signature, String definition
    ) {
        // entry for slash commands
        public Entry(String name, String description) {
            this(
                "whiz", 0, "slash command", "whiz:" + name, null, name,
                null, null, List.of(name), description
            );
        }
    }
    
    private Viewer viewer;

    public Parser(Viewer viewer) {
        this.viewer = viewer;
    }

    private int getLineNumber(Document doc, Element entry) {
        int line = Viewer.getLine(doc.body().wholeText(), entry.wholeText());
        return line;
    }

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
            parent = anchor.substring(0, anchor.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException err) {
            // no parent exists for this element
            parent = "";
        }

        return parent;

    }

    private String getName(String anchor) {
        String name;
        try {
            name = anchor.substring(anchor.lastIndexOf(".") + 1);
        } catch (StringIndexOutOfBoundsException err) {
            // no parent exists for this element
            name = anchor;
        }

        return name;

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

        File folder = Path.of(Main.getAppDirectory(), "python-3.14-docs-html", "library").toFile();
        File[] files = folder.listFiles();

        for (File file : files) {
            viewer.loadDocument(file);
            Document doc = viewer.getRawDocument();
            Elements dls = doc.select("dl");
            logger.debug("parsing {} documentation blocks from {}", dls.size(), file);
            for (Element dl : dls) {
                int line = getLineNumber(doc, dl);

                String type = parseType(dl);
                String anchor = parseAnchor(dl);
                List<String> terms = parseSignatures(dl);
                String def = parseDefinition(dl);
                String parent = getParent(anchor);
                String name = getName(anchor);
                String[] keywords = gatherKeywords(parent != "", anchor);

                if (!anchor.equals("") && !type.equals("")) {
                    entries.add(
                        new Entry(file.getName(), line, type, ("python:" + anchor), anchor, name, parent, keywords, terms, def)
                    );
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
