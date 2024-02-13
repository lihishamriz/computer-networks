import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import static org.junit.jupiter.api.Assertions.*;

public class HTTPResponseTest {

    @Test
    public void testGet_OK() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HTTPRequest mockRequest = new HTTPRequest("GET /index.html HTTP/1.1\r\n", null);
        HTTPResponse httpResponse = new HTTPResponse(mockRequest, outputStream);

        httpResponse.generateResponse();

        String responseString = outputStream.toString();
        assertTrue(responseString.contains("HTTP/1.1 200 OK"), "Response should indicate success");
        assertTrue(responseString.contains("Content-Type: text/html"), "Content type should be text/html");
    }

    @Test
    public void testGet_BadRequest() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HTTPRequest mockRequest = new HTTPRequest(" ", null);
        HTTPResponse httpResponse = new HTTPResponse(mockRequest, outputStream);

        httpResponse.generateResponse();

        String responseString = outputStream.toString();
        assertTrue(responseString.contains("HTTP/1.1 400 Bad Request"), "Response should indicate bad request");
    }

    @Test
    public void testGet_NotFound() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HTTPRequest mockRequest = new HTTPRequest("GET /test.html HTTP/1.1\r\n", null);
        HTTPResponse httpResponse = new HTTPResponse(mockRequest, outputStream);

        httpResponse.generateResponse();

        String responseString = outputStream.toString();
        assertTrue(responseString.contains("HTTP/1.1 404 Not Found"), "Response should indicate page not found");
    }

    @Test
    public void testGet_NotImplemented() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HTTPRequest mockRequest = new HTTPRequest("PUT /index.html HTTP/1.1", null);
        HTTPResponse httpResponse = new HTTPResponse(mockRequest, outputStream);

        httpResponse.generateResponse();

        String responseString = outputStream.toString();
        assertTrue(responseString.contains("HTTP/1.1 501 Not Implemented"), "Response should indicate method not implemented");
    }

    @Test
    public void testSendOKResponseWithChunkedEncoding() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HTTPRequest mockRequest = new HTTPRequest("GET /index.html HTTP/1.1\r\nchunked: yes\r\n", null);
        HTTPResponse httpResponse = new HTTPResponse(mockRequest, outputStream);

        httpResponse.generateResponse();

        String responseString = outputStream.toString();
        assertTrue(responseString.contains("HTTP/1.1 200 OK"), "Response should indicate success");
        assertTrue(responseString.contains("Content-Type: text/html"), "Content type should be text/html");
        assertTrue(responseString.contains("Transfer-Encoding: chunked"), "Chunked encoding should be used");
    }
}
