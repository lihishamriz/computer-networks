import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HTTPResponse {
    private HTTPRequest httpRequest;
    private String rootPath;

    private int statusCode;
    private String statusMessage;
    private String contentType;
    private String contentLength;

    public HTTPResponse(HTTPRequest httpRequest, String rootPath) {
        this.httpRequest = httpRequest;
        this.rootPath = rootPath;
    }

    public void generateResponse() {
        String type = httpRequest.getType();
        if (!type.equals("GET") && !type.equals("POST") && !type.equals("HEAD") && !type.equals("TRACE")) {
            statusCode = 501;
            statusMessage = "Not Implemented";
            sendResponse();
            return;
        }

        String requestedPage = httpRequest.getRequestedPage();
        Path filePath = Paths.get(rootPath + requestedPage);
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            statusCode = 404;
            statusMessage = "Not Found";
            sendResponse();
            return;
        }

        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            contentType = getContentType(requestedPage);
            contentLength = String.valueOf(fileBytes.length);

            statusCode = 200;
            statusMessage = "OK";

            // Create HTTP response header
            String responseHeader = "HTTP/1.1 200 OK\r\n" +
                    "content-type: " + contentType + "\r\n" +
                    "content-length: " + contentLength + "\r\n" +
                    "\r\n";

            // Print the response header
            System.out.println("Response Header:");
            System.out.println(responseHeader);

            // Send the response to the client
            System.out.write(responseHeader.getBytes());
            System.out.write(fileBytes);
        } catch (IOException e) {
            statusCode = 500;
            statusMessage = "Internal Server Error";
            sendResponse();
        }
    }

    private void sendResponse() {
        String response = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n\r\n";
        System.out.println("Response Header:");
        System.out.println(response);
    }

    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "html":
                return "text/html";
            case "bmp":
                return "image/bmp";
            case "gif":
                return "image/gif";
            case "png":
                return "image/png";
            case "jpg":
                return "image/jpeg";
            default:
                return "application/octet-stream";
        }
    }

    public static void main(String[] args) {
        // Sample request headers
        String header = "GET index.html HTTP/1.1\n" +
                "Host: www.example.com\n" +
                "User-Agent: Mozilla/5.0\n" +
                "Referer: http://www.google.com\n" +
                "Content-Length: 100\n";
        HTTPRequest httpRequest = new HTTPRequest(header);
        System.out.println(httpRequest.getRequestedPage());
        String rootPath = "www/lab/html/";

        // Sample HTTPRequest object
        HTTPResponse httpResponse = new HTTPResponse(httpRequest, rootPath);

        // Generate and send the response
        httpResponse.generateResponse();
    }
}
