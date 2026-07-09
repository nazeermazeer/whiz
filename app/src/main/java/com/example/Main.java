package com.example;

import static dev.tamboui.toolkit.Toolkit.*;
import dev.tamboui.style.Style;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.widgets.paragraph.Paragraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;

import dev.tamboui.toolkit.elements.MarkupTextAreaElement;
import dev.tamboui.widgets.common.ScrollBarPolicy;

import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.widgets.input.TextInput;
import dev.tamboui.widgets.input.TextInputState;
import java.awt.Color;


public class Main extends ToolkitApp {
    private static final Path PATH = Path.of("app/src/main/java/com/example/functions.html");
    private static final String TEXT = getText();
    private final TextInputState searchState = new TextInputState();


    @Override
    protected Element render() {
        return panel(panel(document), panel(searchbar));
    }

    private final MarkupTextAreaElement document = markupTextArea(TEXT)
            .wrapWord()
            .title(PATH.getFileName().toString())
            .rounded()
            .scrollbar(ScrollBarPolicy.AS_NEEDED)
            .id("document")
            .focusable();

    private final Element searchbar = panel("Search Example",
            textInput(searchState)
                .placeholder("Type to search..."),
                spacer(),
            text("Searching for: ")
        )            .rounded();


    public static String getText() {
        File html = new File("app/src/main/java/com/example/functions.html");
        String text;

        try {
            Document doc = Jsoup.parse(html, "UTF-8");
            text = doc.body().text();
        } catch (IOException err) {
            text = "could not read file";
        } 

        return text;
    }


    public static void main(String[] args) throws Exception {
        new Main().run();
    }
}




// 
//     // 1. Maintain the state of your search bar input


//     @Override
//     protected Element render() {
//         return panel("Search Example",
//             // 2. Render the text input with a placeholder
//             textInput(searchState)
//                 .placeholder("Type to search..."),
            
//             // 3. Display the current search term dynamically
//             spacer(),
//             text("Searching for: "));
//     }

//     public static void main(String[] args) throws Exception {
//         new Main().run();
//     }
// }
