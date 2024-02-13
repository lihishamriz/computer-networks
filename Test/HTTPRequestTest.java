import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

public class HTTPRequestTest {

    @Test
    public void testGetRequest() {
        String requestHeader = "GET /index.html HTTP/1.1\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(requestHeader));
        HTTPRequest httpRequest = new HTTPRequest(requestHeader, reader);
        assertEquals("/index.html", httpRequest.getRequestedPage(), "Requested page should be /index.html");
    }

    @Test
    public void testPostRequest() throws IOException {
        String requestHeader = "POST /submit HTTP/1.1\r\nContent-Length: 27\r\n\r\nmessage=hello&subscribe=yes";
        BufferedReader reader = new BufferedReader(new StringReader(requestHeader));
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {}
        HTTPRequest httpRequest = new HTTPRequest(requestHeader, reader);
        HashMap<String, String> expectedParams = new HashMap<>();
        expectedParams.put("message", "hello");
        expectedParams.put("subscribe", "yes");
        assertEquals(expectedParams, httpRequest.getParameters(), "Parameters should be extracted from POST request");
    }
}
