import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.Viewer;

public class ViewerTest {
    public static final String table = "┌──────────────────────────────────────────────────────────────────────────────┐"
                              + "\n" + "│This is a table heading                                                       │"
                              + "\n" + "├──────────────────────────────────────────────────────────────────────────────┤"
                              + "\n" + "│This is a table data cell                                                     │"
                              + "\n" + "└──────────────────────────────────────────────────────────────────────────────┘";
    @Test
    public void testGetTitle_ReturnsValidOutput() {
        File html = new File("src/test/java/sample.html");
        String title = Viewer.getTitle(html);
        assertEquals(title, "This is a title");
    }

    @ParameterizedTest
    @CsvSource({
        "0, This is line 0",
        "1, This is line 1",
        "2, This is line 2"
    })
    public void testGetLine_ReturnsValidOutput(int line, String input) {
        String lines = "This is line 0\n"
                     + "This is line 1\n"
                     + "This is line 2\n";

        int result = Viewer.getLine(lines, input);
        assertEquals(result, line);
    }

    @Test
    public void testGetText_RemovesJunk() {
        File html = new File("src/test/java/sample.html");
        Document doc = Viewer.getText(html);
        assertTrue(!doc.body().wholeText().contains("This heading is outside the section"));
        assertTrue(doc.body().wholeText().contains("This heading is inside the section"));
    }

    @Test
    public void testGetText_EscapesOpeningBrackets() {
        File html = new File("src/test/java/sample.html");
        Document doc = Viewer.getText(html);
        assertTrue(doc.body().wholeText().contains("[["));
    }

    @Test
    public void testGetText_EscapesClosingBrackets() {
        File html = new File("src/test/java/sample.html");
        Document doc = Viewer.getText(html);
        assertTrue(doc.body().wholeText().contains("]]"));
    }

    @Test
    public void testGetText_RendersTable() {
        File html = new File("src/test/java/sample.html");
        Document doc = Viewer.getText(html);
        assertTrue(doc.body().wholeText().replaceAll("\\s+", " ").contains(table.replaceAll("\\s+", " ")));
    }
}
