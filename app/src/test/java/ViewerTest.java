import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;

import org.junit.jupiter.api.Test;

import com.example.Viewer;

public class ViewerTest {
    @Test
    public void testGetTitle_ReturnsValidOutput() {
        File html = new File("src/test/java/sample.html");
        String title = Viewer.getTitle(html);
        assertEquals(title, "This is a title");
    }
}
