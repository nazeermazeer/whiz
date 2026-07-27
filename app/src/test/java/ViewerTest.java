import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.Viewer;

public class ViewerTest {
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
}
