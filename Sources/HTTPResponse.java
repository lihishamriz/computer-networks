import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class HTTPResponse {
    private final HTTPRequest httpRequest;
    private final OutputStream output;
    private final Config config;

    public HTTPResponse(HTTPRequest httpRequest, OutputStream output) {
        this.httpRequest = httpRequest;
        this.output = output;
        this.config = new Config("config.ini");
    }

    public void generateResponse() {
        if (httpRequest.getIsBadRequest()) {
            sendBadRequestResponse();
            return;
        }

        String type = httpRequest.getType();
        switch (type) {
            case "GET":
            case "HEAD":
                handleGetAndHeadRequest();
            break;
            case "POST":
                handlePostRequest();
                break;
            case "TRACE":
                handleTraceRequest();
                break;
            default:
                sendNotImplementedResponse();
        }
    }

    private void handleGetAndHeadRequest() {
        String requestedPage = httpRequest.getRequestedPage();
        if(requestedPage.equals("/")){
            requestedPage = this.config.getDefaultPage();
        }
        Path filePath = Paths.get(this.config.getRoot() + requestedPage);
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendNotFoundResponse();
            return;
        }

        try {
            File file = filePath.toFile();
            byte[] fileBytes = readFile(file);
            String contentType = getContentType(requestedPage);
            String contentLength = String.valueOf(fileBytes.length);

            sendOKResponse(fileBytes, contentType, contentLength, this.httpRequest.getType().equals("GET"));
        } catch (Exception e) {
            sendInternalServerErrorResponse();
        }
    }

    private void handlePostRequest() {
        HashMap<String, String> parameters = this.httpRequest.getParameters();
        if (this.httpRequest.getRequestedPage().equals("/params_info.html")) {
            String htmlContent = "<!DOCTYPE html>\r\n" +
                    "<html>\r\n" +
                    "<head>\r\n" +
                    "    <title>Submission Details</title>\r\n" +
                    "</head>\r\n" +
                    "<body>\r\n" +
                    "    <h1>Form Submission Details</h1>\r\n" +
                    "    <p><strong>Message:</strong> " + parameters.get("message") + "</p>\r\n" +
                    "    <p><strong>Subscribe:</strong> " + parameters.get("subscribe") + "</p>\r\n" +
                    "</body>\r\n" +
                    "</html>";

            byte[] htmlBytes = htmlContent.getBytes(StandardCharsets.UTF_8);
            sendOKResponse(htmlBytes, "text/html", String.valueOf(htmlBytes.length), true);
        }
    }

    private void handleTraceRequest() {
        String raw = this.httpRequest.getRawRequest();
        sendOKResponse(raw.getBytes(StandardCharsets.UTF_8), "text/plain", String.valueOf(raw.length()), true);
    }

    private void sendOKResponse(byte[] fileBytes, String contentType, String contentLength, Boolean includeBody) {
        String responseHeader = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "\r\n";

        System.out.println("Response Header:");
        System.out.println(responseHeader);

        try {
            output.write(responseHeader.getBytes());
            if (includeBody) {
                output.write(fileBytes);
            }
            output.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void sendBadRequestResponse() {
        sendResponse(400, "Bad Request");
    }

    private void sendNotFoundResponse() {
        sendResponse(404, "Not Found");
    }

    private void sendInternalServerErrorResponse() {
        sendResponse(500, "Internal Server Error");
    }

    private void sendNotImplementedResponse() {
        sendResponse(501, "Not Implemented");
    }

    private void sendResponse(int statusCode, String statusMessage) {
        String response = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n\r\n";
        System.out.println("Response Header:");
        System.out.println(response);

        try {
            output.write(response.getBytes());
        } catch (IOException ioException) {
            sendInternalServerErrorResponse();
        }
    }

    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "html" -> "text/html";
            case "bmp" -> "image/bmp";
            case "gif" -> "image/gif";
            case "png" -> "image/png";
            case "jpg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }

    private byte[] readFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bFile = new byte[(int)file.length()];

            // read until the end of the stream.
            while(fis.available() != 0) {
                fis.read(bFile, 0, bFile.length);
            }

            return bFile;
        }
        catch(FileNotFoundException e) {
            sendNotFoundResponse();
        } catch(IOException e) {
            sendInternalServerErrorResponse();
        }
        return new byte[0];
    }
}
