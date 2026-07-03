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
        try {
            File html = new File("app/src/main/java/com/example/functions.html");
            Document doc = Jsoup.parse(html, "UTF-8");
            String text = doc.body().text();

            ObjectMapper mapper = new ObjectMapper();

            Elements dls = doc.select("dl");
            Element dl;

            List<Definition> jsonvalues = new ArrayList<>();

            for (int i = 0; i < dls.size(); i++) {
                dl = dls.get(i);
                List<String> terms = new ArrayList<>();
                String def = "";
                if (dl != null) {
                    for (Element element : dl.children()) {
                        if (element.tagName().equals("dt")) {
                            terms.add(element.text());
                        } else if (element.tagName().equals("dd")) {
                            def = element.text();
                        }
                    }
                }
                jsonvalues.add(new Definition(terms, def));
            }

            String jsonstr = mapper.writeValueAsString(jsonvalues);

            File outputfile = new File("app/src/main/java/com/example/functions.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(outputfile, jsonvalues);

            System.out.println(jsonstr); 


            
        } catch (IOException err) {
            System.out.println("Cannot read file");
        }

        // System.out.println(text);
    }
}
