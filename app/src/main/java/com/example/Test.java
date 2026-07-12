package com.example;

// import static dev.tamboui.toolkit.Toolkit.*;

// import dev.tamboui.toolkit.app.ToolkitApp;
// import dev.tamboui.toolkit.element.Element;

// import java.io.File;
// import java.io.FileReader;
// import java.io.IOException;
// import java.nio.file.Path;

// import org.jsoup.nodes.Document;
// import org.jsoup.Jsoup;

// import dev.tamboui.toolkit.elements.MarkupTextAreaElement;
// import dev.tamboui.widgets.block.BorderType;
// import dev.tamboui.widgets.common.ScrollBarPolicy;

// import dev.tamboui.widgets.input.TextInputState;

// import java.io.BufferedReader;
// import java.util.ArrayList;
// import java.security.SecureRandom;
// import java.util.List;

// import org.jsoup.select.Elements;


// public class Main extends ToolkitApp {
//     private static final Path PATH = Path.of("app/src/main/java/com/example/functions.html");
//     private static final String TEXT = getText();
//     private final TextInputState searchState = new TextInputState();


//     @Override
//     protected Element render() {
//         return panel(PATH.getFileName().toString(), panel(document).borderType(BorderType.NONE), panel(searchbar)).borderType(BorderType.NONE);
//     }

//     private final MarkupTextAreaElement document = markupTextArea(TEXT)
//             .wrapWord()
//             .scrollbar(ScrollBarPolicy.AS_NEEDED)
//             .borderType(BorderType.NONE)
//             .id("document")
//             .focusable();

//     private final Element searchbar = 
//             textInput(searchState)
//                 .placeholder(this.getRubbishText() + "...");


//     public static String getText() {
//         File html = new File("app/src/main/java/com/example/functions.html");
//         String text;

//         try {
//             Document doc = Jsoup.parse(html, "UTF-8");
//             doc.outputSettings().prettyPrint(false);

//             Elements tables = doc.select("table");
//             for (org.jsoup.nodes.Element table : tables) {
//                 String renderedTable = renderTableAsText(table);
//                 table.replaceWith(new org.jsoup.nodes.TextNode(renderedTable));
//         }


//             text = doc.body().wholeText();
//         } catch (IOException err) {
//             text = "could not read file";
//         } 

//         return text;
//     }

//     private static String renderTableAsText(org.jsoup.nodes.Element table) {
//         List<List<String>> tableData = new ArrayList<>();
//         int maxColumns = 0;

//         Elements rows = table.select("tr");
//         for (org.jsoup.nodes.Element row : rows) {
//             List<String> rowCells = new ArrayList<>();
//             Elements cells = row.select("th, td");
//             for (org.jsoup.nodes.Element cell : cells) {
//                 rowCells.add(cell.text());
//             }
//             tableData.add(rowCells);
//             if (rowCells.size() > maxColumns) {
//                 maxColumns = rowCells.size();
//             }
//         }

//         int[] colWidths = new int[maxColumns];
//         for (List<String> row : tableData) {
//             for (int i = 0; i < row.size(); i++) {
//                 if (row.get(i).length() > colWidths[i]) {
//                     colWidths[i] = row.get(i).length();
//                 }
//             }
//         }

//         StringBuilder sb = new StringBuilder();
//         sb.append("\n"); 
//         for (List<String> row : tableData) {
//             for (int i = 0; i < maxColumns; i++) {
//                 String cellText = (i < row.size()) ? row.get(i) : "";
//                 // Pad the cell to match the column width
//                 sb.append(String.format("%-" + (colWidths[i] + 4) + "s", cellText));
//             }
//             sb.append("\n");
//         }
//         sb.append("\n");
//         return sb.toString();
//     }

//     public String getRubbishText() {
//         SecureRandom myrandom = new SecureRandom();
//         List<String> entries = new ArrayList<>();

//         try (BufferedReader br = new BufferedReader(new FileReader("app/src/main/java/com/example/rubbish.txt"))) {
//             String line;
//             while ((line = br.readLine()) != null) {
//                 entries.add(line);
//             }
//         } catch (IOException err) {
//             entries.add("rubbish is not rubbishing");
//         }

//         int randomIndex = myrandom.nextInt(entries.size());
//         String line = entries.get(randomIndex);
    

//         return line;
//     }


//     public static void main(String[] args) throws Exception {
//         new Main().run();
//     }
// }

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Test {
    public static void main(String[] args) {
        String html = "<table>" +
                      "<tr><th>Name</th><th>City</th></tr>" +
                      "<tr><td>l</td><td>Ne</td></tr>" +
                      "<tr><td>Li</td><td>San</td></tr>" +
                      "</table>";

        Document doc = Jsoup.parse(html);
        Element table = doc.select("table").first();
        
        if (table == null) return;

        Elements rows = table.select("tr");
        int[] maxLengths = null;

        for (Element row : rows) {
            // Include both <th> and <td> for headers or data
            Elements cells = row.select("th, td");
            
            if (maxLengths == null) {
                maxLengths = new int[cells.size()];
            }

            for (int i = 0; i < cells.size(); i++) {
                // .text() strips out HTML tags and normalizes whitespace
                String cellText = cells.get(i).text();
                int textLength = cellText.length();
                
                if (textLength > maxLengths[i]) {
                    maxLengths[i] = textLength;
                }
            }
        }

        // Print the results
        if (maxLengths != null) {
            for (int i = 0; i < maxLengths.length; i++) {
                System.out.println("Column " + (i + 1) + " max length: " + maxLengths[i]);
            }
        }
    }
}
