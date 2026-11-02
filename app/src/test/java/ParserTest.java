import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import com.example.Parser;
import com.example.Viewer;

public class ParserTest {
    @Test
    public void testParserExtractsEntryFieldsFromDocumentationElement() throws Exception {
        Element entry = Jsoup.parse(
                "<dl class='py function'>"
                        + "<dt id='pathlib.Path.exists'>Path.exists() &#182;</dt>"
                        + "<dd>Return whether this path exists.</dd>"
                        + "</dl>").selectFirst("dl");
        Parser parser = new Parser(new Viewer());

        assertEquals("function", invoke(parser, "parseType", entry));
        assertEquals("pathlib.Path.exists", invoke(parser, "parseAnchor", entry));
        assertEquals("Return whether this path exists.", invoke(parser, "parseDefinition", entry));
        assertEquals(List.of("Path.exists() "), invoke(parser, "parseSignatures", entry));
        assertEquals("pathlib.Path", invoke(parser, "getParent", "pathlib.Path.exists"));
        assertEquals("exists", invoke(parser, "getName", "pathlib.Path.exists"));
        assertArrayEquals(
                new String[] {"pathlib.Path.exists", "exists", "exists()"},
                invoke(parser, "gatherKeywords", true, "pathlib.Path.exists"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(Parser parser, String methodName, Object... arguments) throws Exception {
        Class<?>[] parameterTypes = new Class<?>[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            parameterTypes[i] = arguments[i].getClass();
        }

        Method method = Parser.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(parser, arguments);
    }
}
