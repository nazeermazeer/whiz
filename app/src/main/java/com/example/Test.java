package com.example;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Test {


    public static void main(String[] args) {
        try {
            File html = new File("app/src/main/java/com/example/functions.html");

            Document doc = Jsoup.parse(html);

            doc.outputSettings().prettyPrint(false);



            Element table = doc.select("table").first();




            if (table != null) {
                Elements rows = table.select("tr");
                for (int i = 0; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    Elements cells = row.select("th, td");
                    
                    // Print cell boundaries
                    System.out.print("| ");
                    for (Element cell : cells) {
                        System.out.print(String.format("%-12s | ", cell.wholeText()));
                    }
                    System.out.println();

                    // Add a Markdown separator line right after the header row
                    if (i == 0) {
                        System.out.print("| ");
                        for (int j = 0; j < cells.size(); j++) {
                            System.out.print("------------ | ");
                        }
                        System.out.println();
                }
            }
        }
        } catch (IOException err) {
            System.out.println("IO Exception");
        }
    }
}


