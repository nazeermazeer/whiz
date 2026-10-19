import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.Viewer;

public class ParserTest {
    @Test
    public void testGetRawDocument_EscapesOpeningBrackets() {
        File html = new File("src/test/java/sample.html");
        Viewer viewer = new Viewer();
        viewer.loadDocument(html);

        Document doc = viewer.getRawDocument();

        assertTrue(doc.body().wholeText().contains("[["));
    }

}
