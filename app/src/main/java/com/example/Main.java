package com.example;

import static dev.tamboui.toolkit.Toolkit.*;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;

public class Main extends ToolkitApp {
    @Override
    protected Element render() {
        return panel("Hello",
            text("Welcome to TamboUI!").bold().cyan(),
            spacer(),
            text("Press 'q' to quit").dim()
        ).rounded();
    }

    public static void main(String[] args) throws Exception {
        new Main().run();
    }
}