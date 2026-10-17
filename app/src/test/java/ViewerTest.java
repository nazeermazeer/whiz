import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

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
    public void testGetDocumentTitle_ReturnsValidOutput() {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);

        String title = viewer.getDocumentTitle();

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
    public void testGetRawDocument_RemovesJunk() {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);
        
        Document doc = viewer.getRawDocument();

        assertTrue(!doc.body().wholeText().contains("This heading is outside the section"));
        assertTrue(doc.body().wholeText().contains("This heading is inside the section"));
    }

    @Test
    public void testGetRawDocument_EscapesOpeningBrackets() {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);

        Document doc = viewer.getRawDocument();

        assertTrue(doc.body().wholeText().contains("[["));
    }

    @Test
    public void testGetRawDocument_EscapesClosingBrackets() {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);

        Document doc = viewer.getRawDocument();

        assertTrue(doc.body().wholeText().contains("]]"));
    }

    @Test
    public void testGetRawDocument_RendersTable() {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);
    
        Document doc = viewer.getRawDocument();

        assertTrue(doc.body().wholeText().replaceAll("\\s+", " ").contains(table.replaceAll("\\s+", " ")));
    }

    @Test
    public void testGetRawDocument_DoesNotGetStylizedDocument() throws IOException {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);

        Document doc = viewer.getRawDocument();

        assertTrue(doc.body().wholeText().contains("boring italic boring bold boring strong boring anchor boring"));
    }

    @Test
    public void testGetStylizedDocument_MarksItalics() throws IOException {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);

        Document doc = viewer.getStylizedDocument();

        assertTrue(doc.body().wholeText().contains("boring [italic]italic[/italic] boring"));
    }

    @Test
    public void testGetStylizedDocument_MarksBold() throws IOException {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);

        Document doc = viewer.getStylizedDocument();

        assertTrue(doc.body().wholeText().contains("boring [bold]bold[/bold] boring"));
    }

    @Test
    public void testGetStylizedDocument_MarksStrong() throws IOException {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);

        Document doc = viewer.getStylizedDocument();

        assertTrue(doc.body().wholeText().contains("boring [bold]strong[/bold] boring"));
    }

    @Test
    public void testGetStylizedDocument_MarksLinks() throws IOException {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);

        Document doc = viewer.getStylizedDocument();

        assertTrue(doc.body().wholeText().contains("boring [action=#anchor]anchor[/action] boring"));
    }

    @Test
    public void testGetStylizedDocument_MarksUnorderedLists() throws IOException {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);

        Document doc = viewer.getStylizedDocument();

        assertTrue(doc.body().wholeText().contains("• ul item 1"));
        assertTrue(doc.body().wholeText().contains("• ul item 2"));
        assertTrue(doc.body().wholeText().contains("• ul item 3"));
    }

    @Test
    public void testGetStylizedDocument_MarksOrderedLists() throws IOException {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);

        Document doc = viewer.getStylizedDocument();
        
        assertTrue(doc.body().wholeText().contains("1. ol item 1"));
        assertTrue(doc.body().wholeText().contains("2. ol item 2"));
        assertTrue(doc.body().wholeText().contains("3. ol item 3"));
    }

}
