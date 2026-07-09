package com.example;

import static dev.tamboui.toolkit.Toolkit.*;

import dev.tamboui.layout.Constraint;
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
import dev.tamboui.widgets.block.Block;
import dev.tamboui.widgets.block.BorderType;
import dev.tamboui.widgets.block.Borders;
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
        return panel(PATH.getFileName().toString(), panel(document).borderType(BorderType.NONE), panel(searchbar));
        // return column(panel(document).borderType(BorderType.NONE).length(10), spacer(), panel(searchbar));

    }

    private final MarkupTextAreaElement document = markupTextArea(TEXT)
            .wrapWord()
            .scrollbar(ScrollBarPolicy.AS_NEEDED)
            .borderType(BorderType.NONE)
            .id("document")
            .focusable();

    private final Element searchbar = 
            textInput(searchState)
                .placeholder("Type to search...")
                    ;


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
