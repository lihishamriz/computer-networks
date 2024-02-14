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

    @Test
    public void testGetRequestWithParameters() {
        String requestHeader = "GET /index.html?param1=value1&param2=value2 HTTP/1.1\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(requestHeader));
        HTTPRequest httpRequest = new HTTPRequest(requestHeader, reader);
        HashMap<String, String> expectedParams = new HashMap<>();
        expectedParams.put("param1", "value1");
        expectedParams.put("param2", "value2");
        assertEquals(expectedParams, httpRequest.getParameters(), "Parameters should be extracted from GET request");
    }

    @Test
    public void testPostRequestWithNoParameters() throws IOException {
        String requestHeader = "POST /submit HTTP/1.1\r\nContent-Length: 0\r\n\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(requestHeader));
        HTTPRequest httpRequest = new HTTPRequest(requestHeader, reader);
        assertTrue(httpRequest.getParameters().isEmpty(), "No parameters should be extracted from POST request with no body");
    }

    @Test
    public void testChunkedRequest() throws IOException {
        String requestHeader = "GET /chunked HTTP/1.1\r\nchunked: yes\r\n\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(requestHeader));
        HTTPRequest httpRequest = new HTTPRequest(requestHeader, reader);
        assertTrue(httpRequest.getIsChunked(), "Chunked flag should be true for chunked request");
    }
}
