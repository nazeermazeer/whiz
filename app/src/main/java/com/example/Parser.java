package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.File;
import java.io.IOException;
import org.jsoup.select.Elements;


public class Parser {
    public static void main(String[] args) {
        String text;
        try {
            File html = new File("app/src/main/java/com/example/functions.html");
            Document doc = Jsoup.parse(html, "UTF-8");
            text = doc.body().text();

            Elements dls = doc.select("dl");
            Element dl;
            for (int i = 0; i < dls.size(); i++) {
                dl = dls.get(i);
                if (dl != null) {
                    // Loop through all <dt> and <dd> tags in order
                    for (Element element : dl.children()) {
                        if (element.tagName().equals("dt")) {
                            System.out.println("Term: " + element.text());
                        } else if (element.tagName().equals("dd")) {
                            System.out.println("Description: " + element.text());
                        }
                    }
                }
            }
        } catch (IOException err) {
            text = "could not read file";
        }

        // System.out.println(text);
    }
}
